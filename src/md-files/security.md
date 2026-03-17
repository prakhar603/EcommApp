# EcommApp – Security Practices

---

## 1. Authentication & Authorization

### 1.1 Authentication Mechanism — HTTP Basic over Spring Security

The application uses **Spring Security's HTTP Basic authentication** with a stateless session policy. Every inbound request (except `POST /users/register`) must include an `Authorization: Basic <base64(email:password)>` header.

**How it works at runtime:**

```
Client Request
  │  Authorization: Basic cHJha2hhckBleC5jb206cGFzc3dvcmQ=
  ▼
┌──────────────────────────────────────────────┐
│         BasicAuthenticationFilter             │
│   (provided by Spring Security filter chain)  │
│   Extracts email + password from header       │
└───────────────────┬──────────────────────────┘
                    ▼
┌──────────────────────────────────────────────┐
│         DaoAuthenticationProvider              │
│   Calls UserServiceImpl.loadUserByUsername()   │
│   Fetches user from DB by email               │
│   Compares submitted password against          │
│   stored BCrypt hash via PasswordEncoder       │
└───────────────────┬──────────────────────────┘
                    ▼
           ┌───────────────┐
           │  Match?        │
           │  Yes → 200+    │  Principal injected into SecurityContext
           │  No  → 401     │  "Unauthorized" response
           └───────────────┘
```

**Key configuration (from `SecurityConfig.java`):**

| Setting | Value | Security Implication |
|---------|-------|----------------------|
| `csrf().disable()` | CSRF disabled | Safe because the API is stateless (no cookies/sessions to exploit); standard practice for REST APIs consumed by non-browser clients |
| `SessionCreationPolicy.STATELESS` | No server-side session | Each request is independently authenticated; eliminates session fixation and session hijacking attack vectors |
| `httpBasic()` | HTTP Basic scheme | Credentials in every request; must be paired with TLS in production to prevent eavesdropping |
| `antMatchers(POST, "/users/register").permitAll()` | Registration is public | Only this specific method + path is open; everything else is deny-by-default |
| `.anyRequest().authenticated()` | Default-deny policy | Any endpoint not explicitly whitelisted requires valid credentials |

### 1.2 Authorization

The current system assigns a single role (`USER`) to all authenticated users via `loadUserByUsername()`:

```java
return User.builder()
    .username(user.getEmail())
    .password(user.getPassword())
    .roles("USER")
    .build();
```

There is no role differentiation between consumers and administrators today. This is a recognized simplification — in a production Spring Security setup, role-based authorization would be added via:
- `@PreAuthorize("hasRole('ADMIN')")` on product mutation endpoints
- A `roles` column on the `Users` entity
- `antMatchers` rules in `SecurityFilterChain` segmented by role

### 1.3 Identity Resolution in Controllers

Authenticated controllers receive the user's identity through Spring's `Principal` object, which is injected by the framework after successful authentication:

```java
public ResponseEntity<CartResponse> getCart(Principal principal) {
    return ResponseEntity.ok(cartService.getCart(principal.getName()));
    // principal.getName() returns the authenticated email
}
```

This ensures the application never trusts client-supplied user identifiers for ownership — the user identity always comes from the verified authentication context.

---

## 2. Encryption & Hashing

### 2.1 Password Hashing — BCrypt

All user passwords are hashed before storage using `BCryptPasswordEncoder`:

```java
.password(passwordEncoder.encode(request.getPassword()))
```

| Property | Value |
|----------|-------|
| Algorithm | BCrypt (adaptive hashing) |
| Salt | Automatically generated per password (embedded in hash output) |
| Work factor | Default (10 rounds = 2^10 = 1024 iterations) |
| Reversibility | One-way; passwords cannot be recovered from stored hashes |
| Comparison | `BCryptPasswordEncoder.matches()` — constant-time comparison to prevent timing attacks |

**Why BCrypt matters in production:**
BCrypt is intentionally slow and computationally expensive, making brute-force and rainbow table attacks impractical. The automatic per-password salt means two users with the same password produce different hashes, defeating pre-computed hash tables.

### 2.2 Password Never Exposed in Responses

The `UserResponse` DTO deliberately excludes the password field:

```java
public class UserResponse {
    private Integer id;
    private String email;
    private String firstName;
    private Integer age;
    private String gender;
    // No password field
}
```

Even if an attacker intercepts an API response, the password hash is never present. The `toResponse()` mapping function acts as a one-way gate — entity fields are selectively copied, and sensitive data is structurally excluded.

### 2.3 Transport Encryption (Production Requirement)

HTTP Basic transmits credentials as Base64-encoded plaintext (not encrypted). In production, **TLS (HTTPS) is mandatory** to encrypt all traffic in transit. This is typically handled at the infrastructure layer:
- Reverse proxy (Nginx, HAProxy) with TLS termination
- Kubernetes Ingress with TLS certificates
- Cloud load balancer with SSL offloading

---

## 3. Data Validation & Sanitization

### 3.1 Input Validation at the API Boundary

All request DTOs use **Jakarta Bean Validation** annotations, enforced by `@Valid` on controller method parameters. Invalid requests are rejected before reaching any business logic.

| DTO | Field | Constraint | Purpose |
|-----|-------|------------|---------|
| `RegisterRequest` | `email` | `@NotBlank`, `@Email` | Prevents empty or malformed email |
| `RegisterRequest` | `password` | `@NotBlank`, `@Size(min=6)` | Enforces minimum password complexity |
| `RegisterRequest` | `firstName` | `@NotBlank` | Prevents empty names |
| `RegisterRequest` | `age` | `@NotNull`, `@Min(1)` | Prevents null or non-positive age |
| `RegisterRequest` | `gender` | `@NotBlank` | Prevents empty gender |
| `ProductRequest` | `name` | `@NotBlank` | Prevents empty product names |
| `ProductRequest` | `price` | `@NotNull`, `@DecimalMin("0.01")` | Prevents zero or negative pricing |
| `ProductRequest` | `quantity` | `@NotNull`, `@Min(0)` | Prevents negative stock |
| `AddToCartRequest` | `productId` | `@NotNull` | Prevents null reference |
| `AddToCartRequest` | `quantity` | `@NotNull`, `@Min(1)` | Prevents zero-quantity additions |
| `UpdateCartRequest` | `cartItemId` | `@NotNull` | Prevents null reference |
| `UpdateCartRequest` | `quantity` | `@NotNull`, `@Min(1)` | Prevents zero-quantity updates |
| `RemoveFromCartRequest` | `cartItemId` | `@NotNull` | Prevents null reference |

### 3.2 Business-Level Validation (Defense in Depth)

Beyond annotation-based validation, the service layer enforces business rules that act as a second line of defense:

| Check | Location | What it prevents |
|-------|----------|------------------|
| Email uniqueness | `UserServiceImpl.registerUser()` | Duplicate account creation |
| Stock availability | `CartServiceImpl.addToCart()`, `updateCartItem()` | Over-ordering beyond inventory |
| Cart ownership | `CartServiceImpl.removeFromCart()`, `updateCartItem()` | User A manipulating User B's cart |
| Empty cart guard | `OrderServiceImpl.checkout()` | Placing orders with no items |
| Upfront stock validation | `OrderServiceImpl.checkout()` | Partial order creation on stock shortage |

### 3.3 Type Safety as Implicit Sanitization

Jackson's JSON deserialization enforces type constraints at the framework level. Attempting to send a string for an `Integer` field or malformed JSON results in a `400 Bad Request` before controller code runs. The strongly-typed DTO model means freeform string injection into numeric/boolean fields is structurally impossible.

---

## 4. Protection Against Common Attacks

### 4.1 SQL Injection — Protected

| Layer | Protection Mechanism |
|-------|----------------------|
| **Spring Data JPA** | All repository methods (`findById`, `findByEmail`, `save`, etc.) use parameterized queries generated by Hibernate. User input is never concatenated into SQL strings. |
| **JPQL queries** | The custom `@Query("SELECT u FROM Users u WHERE u.email = :email")` in `UserRepository` uses named parameters (`:email`), which Hibernate binds as prepared statement parameters — immune to SQL injection. |
| **No raw SQL** | The codebase contains zero instances of native SQL, `createNativeQuery()`, or string concatenation in queries. |

**How parameterized queries prevent injection:**
```
Attacker input:  admin' OR '1'='1
What Hibernate executes:  SELECT * FROM users WHERE email = ?
Parameter binding:         ? → "admin' OR '1'='1"  (treated as literal string, not SQL)
```

### 4.2 Cross-Site Scripting (XSS) — Mitigated

| Factor | Details |
|--------|---------|
| **JSON-only API** | The application returns `application/json` responses, not HTML. Browsers do not interpret JSON payloads as executable content. |
| **No server-side rendering** | There are no Thymeleaf, JSP, or any template engine views. The attack surface for reflected/stored XSS via server rendering does not exist. |
| **Spring's default content-type** | Spring Boot sets `Content-Type: application/json` on REST responses, preventing browser content-sniffing that could reinterpret JSON as HTML. |

> **Note for production:** If a front-end renders user-supplied data (e.g., product names, descriptions), output encoding must be handled at the front-end layer. The backend should also consider adding `Content-Security-Policy` and `X-Content-Type-Options: nosniff` response headers.

### 4.3 Cross-Site Request Forgery (CSRF) — Not Applicable

CSRF is disabled in `SecurityConfig`:

```java
http.csrf().disable()
```

This is **correct and intentional** for this architecture because:
- The API is stateless — no cookies or sessions are used
- Authentication is via the `Authorization` header, which browsers do not automatically attach to cross-origin requests
- CSRF attacks exploit cookie-based session authentication, which this system does not use

### 4.4 Session Fixation & Session Hijacking — Eliminated

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

With `STATELESS` policy, Spring Security never creates or uses HTTP sessions. No `JSESSIONID` cookie is issued. Both session fixation (attacker pre-sets a session ID) and session hijacking (attacker steals a session ID) are structurally impossible — there is no session to fix or hijack.

### 4.5 Insecure Direct Object Reference (IDOR) — Partially Protected

The cart module implements explicit ownership verification:

```java
if (!item.getCart().getId().equals(cart.getId())) {
    throw new BadRequestException("Cart item does not belong to the current user");
}
```

This prevents User A from modifying User B's cart items by guessing cart item IDs. The user identity is always derived from the `Principal` (authentication context), never from client-supplied user IDs.

> **Gap:** Product endpoints (`/products/{id}`) do not enforce ownership because the current design treats all products as a shared catalog. In a multi-tenant system, product-level authorization would need to be added.

### 4.6 Mass Assignment — Protected

The application uses dedicated **request DTOs** (`RegisterRequest`, `ProductRequest`, `AddToCartRequest`, etc.) rather than binding JSON directly onto JPA entities. This means:
- An attacker cannot inject fields like `id`, `password`, or `role` into a product update request
- Only the fields explicitly declared in the DTO are accepted
- The manual `toResponse()` / entity-building mapping controls exactly which fields are read from input and which are written to output

### 4.7 Enumeration Attacks — Partially Exposed

| Endpoint | Risk | Current Behavior |
|----------|------|------------------|
| `POST /users/register` | Email enumeration | Returns `"Email already registered: X"` — reveals whether an email exists in the system |
| `GET /products/{id}` | Product ID enumeration | Returns 404 with `"Product not found with id: X"` — reveals valid vs. invalid IDs |

**Production mitigation:** Use generic error messages (e.g., "Registration failed") and rate limiting to reduce enumeration risk.

---

## 5. Error Handling & Secure Logging

### 5.1 Controlled Error Responses

The `GlobalExceptionHandler` ensures that internal implementation details never leak to clients:

| Exception | HTTP Status | What the client sees | What is hidden |
|-----------|:-----------:|----------------------|----------------|
| `ResourceNotFoundException` | 404 | Descriptive message (e.g., "Product not found with id: 5") | Stack trace, class names, SQL |
| `BadRequestException` | 400 | Business rule message (e.g., "Insufficient stock") | Internal state, entity details |
| `MethodArgumentNotValidException` | 400 | Field-level error map (e.g., `{"email": "Invalid email format"}`) | Bean class names, annotation internals |
| `Exception` (catch-all) | 500 | `"An unexpected error occurred"` | **Entire exception suppressed** — no stack trace, no class name, no SQL error |

The catch-all handler is the most critical security control. In the absence of this handler, Spring Boot's default error page could expose:
- Full Java stack traces
- Hibernate SQL statements
- Internal class paths and package structure
- Database table and column names

### 5.2 Consistent Error Format

All error responses follow a uniform structure:

```json
{
    "timestamp": "2026-03-18T10:30:00.000",
    "status": 404,
    "error": "Not Found",
    "message": "Product not found with id: 5"
}
```

This consistency serves two purposes:
1. **Security** — Clients cannot distinguish between different types of internal failures, reducing information leakage
2. **Observability** — Structured, predictable error payloads are parseable by log aggregation systems (ELK, Splunk, Datadog)

### 5.3 SQL Logging (Development Only)

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

SQL logging is enabled for development debugging. **In production, this must be disabled** — SQL logs can contain:
- Table and column names (revealing schema to log-access attacks)
- Query patterns (revealing business logic)
- Parameter values in some configurations (potential PII leakage)

**Production recommendation:** Replace with Hibernate statistics via Micrometer metrics, which expose query counts and timings without logging actual SQL.

---

## 6. Database-Level Security

### 6.1 Schema Constraints as Last-Line Defense

Even if application-level validation is bypassed, the database enforces constraints:

| Entity | Constraint | Purpose |
|--------|------------|---------|
| `Users.email` | `unique = true`, `nullable = false` | Prevents duplicate accounts at the DB level |
| `Users.password` | `nullable = false` | Prevents empty password storage |
| `Products.price` | `precision = 10, scale = 2`, `nullable = false` | Prevents financial data corruption |
| `Cart` → `Users` | `unique = true` on `user_id` FK | Prevents multiple carts per user |
| All FKs | `nullable = false` | Prevents orphaned records |

### 6.2 Credential Storage Concern

Database credentials are currently in plaintext in `application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=Ranger@123
```

**This is a development convenience.** Production systems must externalize secrets using:
- Environment variables (`SPRING_DATASOURCE_PASSWORD`)
- Spring Cloud Config with encrypted values
- HashiCorp Vault or AWS Secrets Manager
- Kubernetes Secrets mounted as environment variables

---

## 7. Internal Service-to-Service Security

### 7.1 Current Architecture — Monolith

EcommApp is a single Spring Boot monolith. All module interactions (User → Cart, Cart → Product, Order → Cart → Product) are **in-process Java method calls** within the same JVM. There is no network boundary between services, so service-to-service authentication is not applicable.

### 7.2 Microservice Migration Path

If the modules are decomposed into separate microservices, the following inter-service security patterns from the Spring ecosystem would apply:

| Pattern | Technology | Purpose |
|---------|------------|---------|
| **Service-to-service auth** | OAuth2 Client Credentials / mTLS | Each service authenticates to others using machine credentials, not user credentials |
| **Token propagation** | Spring Security OAuth2 Resource Server | User's JWT token is forwarded from the API gateway to downstream services, preserving identity |
| **API Gateway** | Spring Cloud Gateway | Centralizes authentication, rate limiting, and TLS termination at the edge |
| **Service mesh** | Istio / Linkerd | Transparent mTLS between all services, plus traffic policies and observability |
| **Secret management** | Vault / K8s Secrets | Runtime injection of credentials without baking them into config files or images |

---

## 8. Security Summary & Maturity Assessment

| Category | Current State | Production Readiness |
|----------|:------------:|:-------------------:|
| Authentication | HTTP Basic (stateless) | Functional — upgrade to JWT or OAuth2 for token-based auth |
| Password storage | BCrypt (10 rounds) | Production-ready |
| Authorization | Single role (USER) | Needs role-based access control (ADMIN vs CUSTOMER) |
| SQL injection | Protected (parameterized queries) | Production-ready |
| XSS | Mitigated (JSON API, no HTML rendering) | Add security response headers for defense-in-depth |
| CSRF | Correctly disabled (stateless API) | Production-ready |
| Session attacks | Eliminated (no sessions) | Production-ready |
| IDOR | Protected for cart operations | Extend ownership checks to all user-scoped resources |
| Mass assignment | Protected (DTO pattern) | Production-ready |
| Error information leakage | Controlled (catch-all handler) | Production-ready |
| Transport encryption | Not configured (no TLS) | Must add TLS via reverse proxy or Spring Boot SSL config |
| Credential management | Plaintext in properties file | Must externalize to Vault / env vars / K8s Secrets |
| Input validation | Comprehensive (Bean Validation) | Production-ready |
| Rate limiting | Not implemented | Add via API gateway or Spring `@RateLimiter` |
| Audit logging | Not implemented | Add for compliance (who did what, when) |
