# EcommApp – Requirement Gathering Document

---

## 1. Requirements

### 1.1 Functional Requirements

#### FR-01: User Registration & Authentication

| ID | Requirement | Details |
|----|-------------|---------|
| FR-01.1 | Self-service user registration | Users can register by providing email, first name, password, age, and gender. The system enforces email uniqueness and password minimum length (6 chars). |
| FR-01.2 | Password encryption | All passwords are hashed with BCrypt before storage. Plaintext passwords are never persisted. |
| FR-01.3 | Automatic cart provisioning | A new, empty shopping cart is created and linked to the user at registration time within the same transaction. |
| FR-01.4 | HTTP Basic authentication | Every request (except registration) must include valid email:password credentials in the HTTP Authorization header. |
| FR-01.5 | Profile retrieval | Authenticated users can retrieve their profile information (id, email, name, age, gender). |

#### FR-02: Product Catalog Management

| ID | Requirement | Details |
|----|-------------|---------|
| FR-02.1 | Create product | Authenticated users can create a product with name, price, quantity, and optional description. Price must be > 0; quantity must be >= 0. |
| FR-02.2 | List all products | Returns the full product catalog with id, name, price, quantity, and description for each item. |
| FR-02.3 | Get product by ID | Retrieves a single product by its primary key. Returns 404 if not found. |
| FR-02.4 | Update product | Replaces name, price, quantity, and description for an existing product. Returns 404 if the product does not exist. |
| FR-02.5 | Delete product | Removes a product by ID. Returns 404 if not found; returns 204 No Content on success. |

#### FR-03: Shopping Cart Operations

| ID | Requirement | Details |
|----|-------------|---------|
| FR-03.1 | View cart | Authenticated users can view their cart including all items, per-item subtotals, and the cart total amount. |
| FR-03.2 | Add item to cart | Adds a product to the user's cart with a specified quantity. If the product already exists in the cart, quantities are merged (summed). Stock is validated before acceptance. |
| FR-03.3 | Update cart item quantity | Changes the quantity of an existing cart item. Validates that the new quantity does not exceed available stock. Ownership is verified. |
| FR-03.4 | Remove item from cart | Removes a specific cart item by its ID. Ownership is verified — users cannot manipulate another user's cart items. |
| FR-03.5 | Stock-aware validation | Every add/update operation checks current product stock. Requests exceeding available inventory are rejected with a 400 error. |

#### FR-04: Order & Checkout

| ID | Requirement | Details |
|----|-------------|---------|
| FR-04.1 | Cart-to-order checkout | Converts the user's entire cart into a finalized order. Cannot checkout an empty cart. |
| FR-04.2 | Upfront stock validation | Before any mutation, the system validates stock for ALL cart items. Fails fast if any single item has insufficient inventory. |
| FR-04.3 | Inventory deduction | On successful checkout, product quantities are decremented by the ordered amounts. |
| FR-04.4 | Order total computation | The order total is computed as the sum of (unit price × quantity) across all line items. |
| FR-04.5 | Cart clearing | After a successful order is persisted, the user's cart is emptied. |
| FR-04.6 | Atomic transaction | The entire checkout (validation → order creation → stock deduction → cart clearing) executes within a single database transaction. Any failure rolls back all changes. |

#### FR-05: Input Validation

| ID | Requirement | Details |
|----|-------------|---------|
| FR-05.1 | Bean Validation on all inputs | All request DTOs are validated using Jakarta Bean Validation annotations (`@NotBlank`, `@Email`, `@Size`, `@Min`, `@NotNull`, `@DecimalMin`). |
| FR-05.2 | Structured validation errors | Validation failures return a 400 response with a map of field names to error messages. |

#### FR-06: Error Handling

| ID | Requirement | Details |
|----|-------------|---------|
| FR-06.1 | Resource not found | Missing entities (user, product, cart, cart item) return 404 with a descriptive message. |
| FR-06.2 | Bad request | Business rule violations (duplicate email, insufficient stock, empty cart checkout, ownership mismatch) return 400. |
| FR-06.3 | Unhandled exceptions | Any unexpected exception returns a generic 500 response without leaking internal details. |
| FR-06.4 | Consistent error format | All error responses share a uniform structure: `timestamp`, `status`, `error`, `message`. |

#### FR-07: API Capabilities Summary

| Method | Endpoint | Auth Required | Purpose |
|--------|----------|:-------------:|---------|
| POST | `/users/register` | No | Register a new user |
| GET | `/users/profile` | Yes | Get authenticated user's profile |
| POST | `/products` | Yes | Create a product |
| GET | `/products` | Yes | List all products |
| GET | `/products/{id}` | Yes | Get product by ID |
| PUT | `/products/{id}` | Yes | Update a product |
| DELETE | `/products/{id}` | Yes | Delete a product |
| GET | `/cart` | Yes | View user's cart |
| POST | `/cart/add` | Yes | Add item to cart |
| PUT | `/cart/update` | Yes | Update cart item quantity |
| DELETE | `/cart/remove` | Yes | Remove item from cart |
| POST | `/orders/checkout` | Yes | Checkout cart into an order |

---

### 1.2 Non-Functional Requirements

#### NFR-01: Performance

| ID | Requirement | Details |
|----|-------------|---------|
| NFR-01.1 | Read-only transaction optimization | Read operations (`getCart`, `getAllProducts`, `getProductById`, `getUserProfile`) are annotated with `@Transactional(readOnly = true)`, signaling Hibernate to skip dirty-checking and flush, reducing overhead. |
| NFR-01.2 | Lazy loading | All entity relationships (`@ManyToOne`, `@OneToMany`, `@OneToOne`) use `FetchType.LAZY` to avoid loading the entire object graph on every query. Data is fetched only when explicitly traversed. |
| NFR-01.3 | DTO projection | Controllers never return JPA entities. Lightweight DTOs ensure only necessary fields are serialized, reducing payload size and preventing accidental lazy-loading triggers in the JSON serializer. |
| NFR-01.4 | SQL visibility | `show-sql=true` and `format_sql=true` are enabled, allowing developers to inspect and optimize generated queries during development. |

#### NFR-02: Scalability

| ID | Requirement | Details |
|----|-------------|---------|
| NFR-02.1 | Stateless authentication | HTTP Basic with no server-side sessions (`SessionCreationPolicy.STATELESS`). Any application instance can serve any request, enabling horizontal scaling behind a load balancer with zero session affinity. |
| NFR-02.2 | Embedded server packaging | Spring Boot packages the app as a self-contained JAR with embedded Tomcat. No external application server is required, making it trivially containerizable (Docker) and orchestratable (Kubernetes). |
| NFR-02.3 | Interface-driven decomposition | All services are coded against interfaces (`IUserService`, `IProductService`, `ICartService`, `IOrderService`). Each module can be extracted into a standalone microservice with its own database by replacing the interface binding. |
| NFR-02.4 | Externalized configuration | All environment-specific settings (database URL, credentials, JPA dialect) reside in `application.properties` and can be overridden via Spring profiles, environment variables, or a centralized config server — standard practice for multi-environment and cloud-native deployments. |

#### NFR-03: Reliability & Fault Tolerance

| ID | Requirement | Details |
|----|-------------|---------|
| NFR-03.1 | Transactional integrity | All write operations are wrapped in `@Transactional`. The checkout flow — the most critical path — executes stock validation, order creation, inventory deduction, and cart clearing as a single atomic unit. Any failure triggers a full rollback. |
| NFR-03.2 | Fail-fast stock validation | During checkout, ALL items are validated for stock before any mutation begins. This prevents partial order creation (e.g., deducting stock for 3 of 5 items before discovering the 4th is unavailable). |
| NFR-03.3 | Orphan removal | Cart items and order items use `orphanRemoval = true`, ensuring that removing an item from the parent collection automatically deletes the database row — preventing orphaned records. |
| NFR-03.4 | Global exception safety net | The `GlobalExceptionHandler` catches all unhandled exceptions and returns a controlled 500 response, preventing stack traces or internal state from leaking to clients. |
| NFR-03.5 | Input validation at the boundary | All inputs are validated at the controller layer before reaching business logic, preventing malformed data from propagating through the system. |

#### NFR-04: Logging & Monitoring

| ID | Requirement | Details |
|----|-------------|---------|
| NFR-04.1 | SQL query logging | Hibernate is configured to log all generated SQL statements (`show-sql=true`) with formatted output (`format_sql=true`), enabling query-level debugging and performance profiling. |
| NFR-04.2 | Spring Boot Actuator readiness | The Maven dependency structure supports adding `spring-boot-starter-actuator` for production health checks (`/actuator/health`), metrics (Micrometer), and readiness/liveness probes for Kubernetes. |
| NFR-04.3 | Structured error responses | All exceptions produce timestamped, structured JSON error payloads, making errors parseable by log aggregation tools (ELK, Splunk) and API monitoring platforms. |

---

## 2. System Users & Backend Use Cases

### 2.1 User Types

| User Type | Description | Authentication | Accessible Endpoints |
|-----------|-------------|:--------------:|----------------------|
| **End Consumer (B2C)** | A retail customer who registers, browses products, manages a shopping cart, and places orders. | HTTP Basic (email + password) | `/users/register`, `/users/profile`, `/products` (read), `/cart/*`, `/orders/checkout` |
| **Catalog Administrator** | An internal user or system responsible for managing the product catalog — creating, updating, and deleting products. | HTTP Basic (email + password) | `/products` (full CRUD) |
| **Internal / Upstream System** | Any backend service, batch job, or integration layer that calls the API programmatically (e.g., an inventory sync tool or a front-end BFF). | HTTP Basic (service account) | All authenticated endpoints |

> **Note:** The current system uses a single `USER` role for all authenticated users. Role-based access control (ADMIN vs. CUSTOMER) is an identified extension point.

---

### 2.2 Backend Use Cases

#### UC-01: User Registration

| Aspect | Details |
|--------|---------|
| **Actor** | Unauthenticated end consumer |
| **Input** | `POST /users/register` with JSON body: `email`, `firstName`, `password`, `age`, `gender` |
| **Processing** | 1. Validate input fields (format, constraints) → 2. Check email uniqueness → 3. Hash password with BCrypt → 4. Build `Users` entity → 5. Create empty `Cart` and link to user → 6. Persist user (cascades cart) → 7. Map to `UserResponse` |
| **Output** | `201 Created` — `UserResponse { id, email, firstName, age, gender }` |
| **Error paths** | 400 if validation fails; 400 if email already registered |

#### UC-02: User Profile Retrieval

| Aspect | Details |
|--------|---------|
| **Actor** | Authenticated end consumer |
| **Input** | `GET /users/profile` (principal extracted from auth header) |
| **Processing** | 1. Extract email from `Principal` → 2. Lookup user by email → 3. Map to `UserResponse` |
| **Output** | `200 OK` — `UserResponse { id, email, firstName, age, gender }` |
| **Error paths** | 401 if unauthenticated; 404 if user not found |

#### UC-03: Create Product

| Aspect | Details |
|--------|---------|
| **Actor** | Catalog administrator |
| **Input** | `POST /products` with JSON body: `name`, `price`, `quantity`, `description` (optional) |
| **Processing** | 1. Validate input → 2. Build `Products` entity → 3. Persist → 4. Map to `ProductResponse` |
| **Output** | `201 Created` — `ProductResponse { id, name, price, quantity, description }` |
| **Error paths** | 400 if validation fails; 401 if unauthenticated |

#### UC-04: Browse Product Catalog

| Aspect | Details |
|--------|---------|
| **Actor** | Authenticated end consumer or internal system |
| **Input** | `GET /products` (list all) or `GET /products/{id}` (single) |
| **Processing** | 1. Fetch all products or find by ID → 2. Map each entity to `ProductResponse` |
| **Output** | `200 OK` — `List<ProductResponse>` or single `ProductResponse` |
| **Error paths** | 401 if unauthenticated; 404 if product ID not found |

#### UC-05: Update Product

| Aspect | Details |
|--------|---------|
| **Actor** | Catalog administrator |
| **Input** | `PUT /products/{id}` with JSON body: `name`, `price`, `quantity`, `description` |
| **Processing** | 1. Validate input → 2. Find product by ID → 3. Overwrite fields → 4. Persist → 5. Map to `ProductResponse` |
| **Output** | `200 OK` — `ProductResponse` |
| **Error paths** | 400 if validation fails; 404 if product not found |

#### UC-06: Delete Product

| Aspect | Details |
|--------|---------|
| **Actor** | Catalog administrator |
| **Input** | `DELETE /products/{id}` |
| **Processing** | 1. Verify product exists → 2. Delete from database |
| **Output** | `204 No Content` |
| **Error paths** | 404 if product not found |

#### UC-07: View Cart

| Aspect | Details |
|--------|---------|
| **Actor** | Authenticated end consumer |
| **Input** | `GET /cart` (principal extracted from auth header) |
| **Processing** | 1. Resolve user from email → 2. Fetch cart with items → 3. Compute per-item subtotals and cart total → 4. Map to `CartResponse` |
| **Output** | `200 OK` — `CartResponse { cartId, userId, items[ ], totalAmount }` |
| **Error paths** | 401 if unauthenticated; 404 if user or cart not found |

#### UC-08: Add Item to Cart

| Aspect | Details |
|--------|---------|
| **Actor** | Authenticated end consumer |
| **Input** | `POST /cart/add` with JSON body: `productId`, `quantity` |
| **Processing** | 1. Resolve user's cart → 2. Find product by ID → 3. Check if product already in cart → 4a. If yes: merge quantities and validate stock → 4b. If no: validate stock and create new `CartItem` → 5. Save cart → 6. Return updated `CartResponse` |
| **Output** | `200 OK` — `CartResponse` with updated items and total |
| **Error paths** | 400 if insufficient stock; 404 if product not found |

#### UC-09: Update Cart Item Quantity

| Aspect | Details |
|--------|---------|
| **Actor** | Authenticated end consumer |
| **Input** | `PUT /cart/update` with JSON body: `cartItemId`, `quantity` |
| **Processing** | 1. Resolve user's cart → 2. Find cart item by ID → 3. Verify ownership (item belongs to user's cart) → 4. Validate new quantity against product stock → 5. Update quantity → 6. Return `CartResponse` |
| **Output** | `200 OK` — `CartResponse` |
| **Error paths** | 400 if ownership mismatch or insufficient stock; 404 if cart item not found |

#### UC-10: Remove Item from Cart

| Aspect | Details |
|--------|---------|
| **Actor** | Authenticated end consumer |
| **Input** | `DELETE /cart/remove` with JSON body: `cartItemId` |
| **Processing** | 1. Resolve user's cart → 2. Find cart item by ID → 3. Verify ownership → 4. Remove item from cart collection → 5. Delete cart item from database → 6. Return `CartResponse` |
| **Output** | `200 OK` — `CartResponse` with item removed |
| **Error paths** | 400 if ownership mismatch; 404 if cart item not found |

#### UC-11: Checkout (Cart → Order)

| Aspect | Details |
|--------|---------|
| **Actor** | Authenticated end consumer |
| **Input** | `POST /orders/checkout` (principal extracted from auth header; no request body) |
| **Processing** | 1. Resolve user → cart → cart items → 2. Reject if cart is empty → 3. **Validate stock for ALL items** (fail-fast) → 4. Create `Order` entity with timestamp → 5. For each cart item: deduct product stock, create `OrderItem`, add to order → 6. Compute and set `totalAmount` → 7. Persist order (cascades order items) → 8. Clear cart items → 9. Save cart → 10. Return `OrderResponse` — **entire flow is atomic (@Transactional)** |
| **Output** | `201 Created` — `OrderResponse { orderId, userId, userEmail, items[ ], totalAmount, createdAt }` |
| **Error paths** | 400 if cart is empty; 400 if any item has insufficient stock; 404 if user or cart not found |

---

## 3. Major Backend Features

### Feature Catalog

| # | Feature Name | Description | Module / Service | Key APIs |
|:-:|--------------|-------------|------------------|----------|
| 1 | **User Registration** | Allows new customers to create an account with validated input, encrypted password storage, and automatic cart provisioning. | `UserServiceImpl` → `UserRepository` | `POST /users/register` |
| 2 | **User Authentication** | Secures all endpoints (except registration) with stateless HTTP Basic authentication. Credentials are verified against BCrypt-hashed passwords via Spring Security's `DaoAuthenticationProvider`. | `SecurityConfig` + `UserServiceImpl` (as `UserDetailsService`) | Every authenticated request |
| 3 | **Profile Management** | Returns the authenticated user's profile data without exposing sensitive fields (password is excluded from the response DTO). | `UserServiceImpl` → `UserRepository` | `GET /users/profile` |
| 4 | **Product Creation** | Enables catalog administrators to add new products with name, price, stock quantity, and optional description. Input is validated at the boundary. | `ProductServiceImpl` → `ProductRepository` | `POST /products` |
| 5 | **Product Catalog Browsing** | Provides read access to the full product list or individual products by ID. Uses read-only transactions for performance. | `ProductServiceImpl` → `ProductRepository` | `GET /products`, `GET /products/{id}` |
| 6 | **Product Update** | Allows full replacement of a product's attributes (name, price, quantity, description). | `ProductServiceImpl` → `ProductRepository` | `PUT /products/{id}` |
| 7 | **Product Deletion** | Removes a product from the catalog by ID with existence verification. | `ProductServiceImpl` → `ProductRepository` | `DELETE /products/{id}` |
| 8 | **Cart Viewing** | Returns the user's persistent cart with item-level details (product info, quantity, subtotal) and a computed cart total. | `CartServiceImpl` → `CartRepository` | `GET /cart` |
| 9 | **Add to Cart** | Adds a product to the cart with stock validation. Intelligently merges quantities if the same product is added multiple times. | `CartServiceImpl` → `CartItemRepository`, `ProductRepository` | `POST /cart/add` |
| 10 | **Update Cart Item** | Modifies the quantity of a specific cart item after verifying ownership and available stock. | `CartServiceImpl` → `CartItemRepository`, `ProductRepository` | `PUT /cart/update` |
| 11 | **Remove from Cart** | Deletes a cart item after ownership verification. Uses JPA orphan removal for clean database hygiene. | `CartServiceImpl` → `CartItemRepository` | `DELETE /cart/remove` |
| 12 | **Order Checkout** | Atomic conversion of a cart into a finalized order: validates all stock upfront, creates order with line items, deducts inventory, computes total, and clears the cart — all within a single transaction. | `OrderServiceImpl` → `OrderRepository`, `ProductRepository`, `CartRepository` | `POST /orders/checkout` |
| 13 | **Input Validation** | All request payloads are validated using Bean Validation annotations on DTOs. Validation failures produce field-level error maps. | All Controllers (via `@Valid`) + `GlobalExceptionHandler` | All mutation endpoints |
| 14 | **Global Exception Handling** | Centralized `@RestControllerAdvice` translates domain exceptions and validation errors into consistent, timestamped JSON error responses with appropriate HTTP status codes. | `GlobalExceptionHandler` | Cross-cutting (all endpoints) |
| 15 | **Transactional Data Integrity** | All service methods run inside Spring-managed transactions. Write operations use read-write transactions; read operations are optimized with `readOnly = true`. | All `*ServiceImpl` classes | Cross-cutting (all endpoints) |
