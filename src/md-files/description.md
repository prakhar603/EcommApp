# EcommApp – Backend System Description

---

## 1. System Objective

EcommApp exists to solve a fundamental business problem: providing a reliable, secure, and self-contained backend for online retail. In a business context, it enables a merchant to onboard customers, manage a product catalog with real-time inventory, let shoppers build carts, and convert those carts into orders — all through a clean REST API. It serves as the transactional backbone that any storefront (web, mobile, or third-party integration) can consume.

---

## 2. Overall System Architecture

The system follows a **layered monolithic architecture** built on the Spring Boot framework:

```
  ┌──────────────────────────────────────────────────────┐
  │                     Client (HTTP)                     │
  └────────────────────────┬─────────────────────────────┘
                           │  HTTP Basic Auth
  ┌────────────────────────▼─────────────────────────────┐
  │              Spring Security Filter Chain              │
  │         (Stateless · BCrypt · DaoAuthProvider)         │
  └────────────────────────┬─────────────────────────────┘
                           │
  ┌────────────────────────▼─────────────────────────────┐
  │                  Controller Layer                      │
  │   UserController · ProductController · CartController  │
  │                   OrderController                      │
  └────────────────────────┬─────────────────────────────┘
                           │  DTOs (Request / Response)
  ┌────────────────────────▼─────────────────────────────┐
  │                   Service Layer                        │
  │    IUserService · IProductService · ICartService       │
  │                  IOrderService                         │
  │          (Interface + Impl, @Transactional)            │
  └────────────────────────┬─────────────────────────────┘
                           │  JPA Entities
  ┌────────────────────────▼─────────────────────────────┐
  │                 Repository Layer                       │
  │       Spring Data JPA (JpaRepository extensions)       │
  └────────────────────────┬─────────────────────────────┘
                           │  JDBC / Hibernate
  ┌────────────────────────▼─────────────────────────────┐
  │                     MySQL 8                            │
  │                  (ecommercedb)                          │
  └──────────────────────────────────────────────────────┘
```

Every layer has a single, well-defined responsibility:

- **Security layer** intercepts all inbound requests, authenticates credentials, and injects the authenticated principal into downstream controllers.
- **Controller layer** accepts HTTP input, delegates to services, and returns structured JSON responses. It owns no business logic.
- **Service layer** encapsulates all business rules — registration constraints, stock validation, cart merging, checkout atomicity.
- **Repository layer** abstracts persistence. Custom query methods are declared as Spring Data JPA derived queries.
- **Database** is MySQL 8 with Hibernate auto-DDL, so the schema evolves alongside the entity model.

---

## 3. High-Level Data Flow

### Registration (public)

```
Client  ──POST /users/register──▶  UserController
        ──▶  UserServiceImpl: validate uniqueness, hash password, create User + empty Cart
        ──▶  UserRepository.save()  ──▶  MySQL
        ◀──  UserResponse (id, email, name)
```

### Authenticated Request Lifecycle

```
Client (email:password via HTTP Basic)
   │
   ▼
SecurityFilterChain  ──▶  DaoAuthenticationProvider
   │                         ──▶  UserServiceImpl.loadUserByUsername()
   │                         ◀──  UserDetails (verified)
   ▼
Controller  ──▶  receives Principal (authenticated email)
   │
   ▼
Service  ──▶  resolves User entity from email
         ──▶  executes business logic (CRUD, cart ops, checkout)
         ──▶  Repository  ──▶  MySQL
   │
   ▼
Client  ◀──  JSON Response (DTO)
```

### Checkout Flow (most complex transaction)

```
OrderServiceImpl.checkout(email):
   1. Resolve User → Cart → CartItems
   2. Validate stock for ALL items upfront (fail-fast)
   3. Build Order + OrderItems
   4. Deduct product stock for each line item
   5. Compute and set total amount
   6. Persist Order (cascades OrderItems)
   7. Clear Cart
   8. Return OrderResponse
   ── entire flow wrapped in @Transactional (all-or-nothing) ──
```

---

## 4. Key Modules and Responsibilities

### User Module
Handles registration and profile retrieval. On registration, it enforces email uniqueness, hashes the password with BCrypt, and automatically provisions an empty shopping cart for the new user. It also implements Spring Security's `UserDetailsService`, making it the bridge between authentication and the domain model.

### Product Module
Provides full CRUD for the product catalog. Each product tracks name, description, price, and available quantity. This module is the source of truth for inventory — both the cart and order modules read from and write to product stock through it.

### Cart Module
Manages per-user persistent shopping carts. It supports adding items (with automatic quantity merging if the same product is added twice), removing items, and updating quantities. Every mutation validates against current product stock before committing. Ownership checks ensure a user can only operate on their own cart items.

### Order Module
Converts a cart into a finalized order. The checkout operation is the system's most critical transaction: it validates all stock upfront, creates the order with line items, deducts inventory, computes the total, and clears the cart — all within a single atomic database transaction. If any step fails, the entire operation rolls back.

### Security Module
A centralized `SecurityConfig` defines the authentication and authorization policy. It uses stateless HTTP Basic authentication with BCrypt password encoding. The only public endpoint is user registration; everything else requires valid credentials. The stateless session policy means no server-side session storage — each request is independently authenticated.

### Exception Handling Module
A `GlobalExceptionHandler` (`@RestControllerAdvice`) provides consistent, structured error responses across the entire API. It maps domain exceptions (`ResourceNotFoundException`, `BadRequestException`) to appropriate HTTP status codes and converts validation failures into field-level error maps.

---

## 5. Scale and Modularity

### Modularity

The codebase is designed for clean separation and future decomposition:

- **Interface-driven services** — Every service is coded against an interface (`IUserService`, `IProductService`, etc.). Implementations can be swapped, decorated, or replaced without touching controllers. This is a textbook application of the Dependency Inversion Principle.

- **DTO isolation** — Controllers never expose JPA entities directly. Dedicated request and response DTOs decouple the API contract from the persistence model. The internal schema can evolve (columns added, tables restructured) without breaking API consumers.

- **Single-responsibility packages** — The codebase is organized into `controller`, `service`, `service/impl`, `repository`, `entity`, `dto`, `exception`, and `config` packages, each owning one concern. This structure maps cleanly onto a future migration to separate microservices (e.g., extracting the Order module into its own deployable).

- **Constructor injection everywhere** — All dependencies are injected via constructors (no field injection), making every component trivially unit-testable with mocks.

### Scalability Readiness

- **Stateless security** — No server-side sessions. Any instance can handle any request, making the application horizontally scalable behind a load balancer with zero session-affinity requirements.

- **Transactional integrity** — Critical operations (checkout) are wrapped in `@Transactional`, ensuring data consistency under concurrent access. This is the foundation upon which optimistic/pessimistic locking or distributed transactions can be layered.

- **Hibernate auto-DDL** — The schema tracks the entity model automatically, simplifying deployment across environments. For production scale, this can be replaced by a migration tool (Flyway or Liquibase) for versioned, repeatable schema changes.

- **Externalized configuration** — Database URL, credentials, and JPA settings are in `application.properties`, making the application ready for environment-specific overrides via Spring profiles, environment variables, or a config server.

- **Container-ready architecture** — The application is a self-contained Spring Boot JAR with an embedded Tomcat server. It requires no external application server, making it trivially containerizable with Docker and deployable onto orchestration platforms like Kubernetes.

---

## 6. Summary

EcommApp is a focused, well-structured e-commerce backend that prioritizes correctness (transactional checkout, stock validation), security (stateless auth, encrypted passwords), and maintainability (layered architecture, interface-driven design). Its monolithic design is deliberate for the current scale, but its internal modularity makes it straightforward to decompose into microservices as business needs grow.
