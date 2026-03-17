# EcommApp – Technologies Used

> This document lists every backend technology present in the project, explains what it is, why this project uses it, and how it contributes to real-world systems. Technologies are grouped by role. Only technologies actually declared in `pom.xml` or `application.properties` are listed as **"In Use"**; technologies that the architecture is ready to adopt but are not yet configured are listed under **"Adoption-Ready"** at the end.

---

## 1. Language & Runtime

### Java 17

| Aspect | Details |
|--------|---------|
| **What it is** | A general-purpose, statically-typed, object-oriented programming language that runs on the Java Virtual Machine (JVM). Version 17 is a Long-Term Support (LTS) release. |
| **Why it is used** | Java 17 provides modern language features (sealed classes, records, pattern matching for `instanceof`, text blocks) alongside the stability and backward compatibility that enterprise systems demand. As an LTS release, it receives security patches and vendor support for years. |
| **How it helps in real-world systems** | The JVM's Just-In-Time (JIT) compiler, garbage collection tuning, and mature ecosystem make Java the dominant language for high-throughput backend services. Its strong type system catches errors at compile time, reducing production bugs. The vast talent pool and library ecosystem lower hiring and development costs at scale. |
| **Version in project** | `17` (declared in `pom.xml` → `<java.version>17</java.version>`) |

---

## 2. Application Framework

### Spring Boot 2.7.8

| Aspect | Details |
|--------|---------|
| **What it is** | An opinionated, convention-over-configuration framework built on top of the Spring Framework. It auto-configures infrastructure (embedded server, data source, security) based on classpath dependencies, eliminating boilerplate XML and manual wiring. |
| **Why it is used** | Spring Boot lets EcommApp define a production-ready REST API, JPA persistence layer, and security configuration with minimal setup. A single `@SpringBootApplication` annotation bootstraps component scanning, auto-configuration, and an embedded Tomcat server. |
| **How it helps in real-world systems** | Spring Boot is the de facto standard for Java microservices and monoliths. Its starter system (`spring-boot-starter-*`) ensures compatible dependency versions, its embedded server eliminates external app server management, and its actuator module provides out-of-the-box health checks and metrics for operations teams. Companies like Netflix, Alibaba, and major banks run Spring Boot in production at massive scale. |
| **Version in project** | `2.7.8` (declared in `pom.xml` → `<spring-boot-starter-parent>`) |

**Starters used in this project:**

| Starter | Purpose |
|---------|---------|
| `spring-boot-starter-web` | REST API layer (embedded Tomcat, Jackson JSON, Spring MVC) |
| `spring-boot-starter-data-jpa` | ORM and database access (Hibernate, Spring Data repositories) |
| `spring-boot-starter-security` | Authentication and authorization (Spring Security filter chain) |
| `spring-boot-starter-validation` | Request payload validation (Jakarta Bean Validation / Hibernate Validator) |
| `spring-boot-starter-test` | Testing framework (JUnit 5, Mockito, Spring Test) |

---

## 3. Web & API Layer

### Spring Web MVC (via `spring-boot-starter-web`)

| Aspect | Details |
|--------|---------|
| **What it is** | Spring's web framework for building RESTful HTTP services. It provides `@RestController`, `@RequestMapping`, request/response body binding, content negotiation, and exception handling. |
| **Why it is used** | Every API endpoint in EcommApp — user registration, product CRUD, cart operations, checkout — is implemented as a Spring MVC controller method. The framework handles HTTP method routing, JSON serialization/deserialization, status code mapping, and `Principal` injection from Spring Security. |
| **How it helps in real-world systems** | Spring MVC is battle-tested for building REST APIs consumed by web apps, mobile clients, and partner integrations. Its annotation-driven model (`@GetMapping`, `@PostMapping`, `@Valid`) keeps controller code declarative and readable. The `@RestControllerAdvice` mechanism enables centralized, consistent error handling across all endpoints — critical for API governance in large organizations. |

### Embedded Apache Tomcat

| Aspect | Details |
|--------|---------|
| **What it is** | A Java servlet container that Spring Boot embeds directly inside the application JAR. It serves HTTP requests without requiring an external application server installation. |
| **Why it is used** | EcommApp runs as a self-contained `java -jar` process on port 8080 (`server.port=8080`). No Tomcat installation, no WAR deployment, no server configuration files. |
| **How it helps in real-world systems** | Embedded servers are foundational to the container-native deployment model. A single JAR with no external dependencies is trivially packageable into a Docker image (`FROM eclipse-temurin:17-jre` + `COPY app.jar` + `ENTRYPOINT`). This simplifies CI/CD pipelines, reduces infrastructure drift, and is a prerequisite for Kubernetes orchestration. |

### Jackson (via `spring-boot-starter-web`)

| Aspect | Details |
|--------|---------|
| **What it is** | A high-performance JSON serialization/deserialization library. It converts Java objects to JSON (responses) and JSON to Java objects (requests) automatically. |
| **Why it is used** | Every request body (`RegisterRequest`, `ProductRequest`, `AddToCartRequest`, etc.) is deserialized from JSON by Jackson, and every response (`UserResponse`, `CartResponse`, `OrderResponse`, etc.) is serialized to JSON. EcommApp relies on Jackson's type-safe binding — a string sent for an `Integer` field is rejected at the framework level before controller code runs. |
| **How it helps in real-world systems** | Jackson is the most widely used JSON library in the Java ecosystem. Its streaming parser handles large payloads efficiently, its annotation system (`@JsonIgnore`, `@JsonProperty`) provides fine-grained control over API contracts, and its module system supports Java 8 date/time, Kotlin, and other type systems out of the box. |

---

## 4. Data Persistence Layer

### Spring Data JPA (via `spring-boot-starter-data-jpa`)

| Aspect | Details |
|--------|---------|
| **What it is** | An abstraction layer on top of JPA (Java Persistence API) that eliminates boilerplate data access code. Developers declare repository interfaces extending `JpaRepository`, and Spring generates the implementation at runtime — including query derivation from method names. |
| **Why it is used** | EcommApp defines five repositories (`UserRepository`, `ProductRepository`, `CartRepository`, `CartItemRepository`, `OrderRepository`) as interfaces with zero implementation code. Methods like `findByEmail(String email)` and `findByCartAndProduct(Cart, Product)` are automatically translated into optimized SQL by Spring Data. Custom JPQL is used where needed (e.g., `@Query("SELECT u FROM Users u WHERE u.email = :email")`). |
| **How it helps in real-world systems** | Spring Data JPA dramatically reduces the data access layer codebase — a typical repository that would require hundreds of lines of JDBC code is replaced by a single interface declaration. It also provides built-in pagination, sorting, auditing, and query optimization. Teams spend time on business logic instead of SQL plumbing. |

### Hibernate ORM 5.x (transitive via `spring-boot-starter-data-jpa`)

| Aspect | Details |
|--------|---------|
| **What it is** | The most widely used JPA implementation. Hibernate maps Java entity classes to database tables, manages object lifecycle (persistence context), generates SQL, handles lazy loading, and manages database schema evolution. |
| **Why it is used** | EcommApp's six entities (`Users`, `Products`, `Cart`, `CartItem`, `Order`, `OrderItem`) are mapped to MySQL tables via JPA annotations (`@Entity`, `@Table`, `@Column`, `@OneToMany`, `@ManyToOne`, etc.). Hibernate generates all DDL and DML statements. The `ddl-auto=update` setting lets Hibernate auto-create and alter tables to match the entity model. |
| **How it helps in real-world systems** | Hibernate provides database portability (switch from MySQL to PostgreSQL by changing the dialect), first-level caching (within a transaction), lazy loading (fetch related entities only when accessed), and dirty checking (only persist changed fields). These features reduce database round trips and simplify multi-table transactional workflows like the checkout flow in this project. |
| **Configuration in project** | `MySQL8Dialect`, `ddl-auto=update`, `show-sql=true`, `format_sql=true` |

### MySQL 8

| Aspect | Details |
|--------|---------|
| **What it is** | An open-source relational database management system (RDBMS) owned by Oracle. MySQL 8 introduced window functions, CTEs, JSON support, and improved security defaults. |
| **Why it is used** | EcommApp stores all persistent data — users, products, carts, cart items, orders, and order items — in a MySQL 8 database (`ecommercedb`). The relational model naturally fits the e-commerce domain: foreign key relationships enforce referential integrity between orders and users, cart items and products, etc. |
| **How it helps in real-world systems** | MySQL powers some of the largest web applications in the world (Facebook, Twitter, Airbnb, Shopify). It provides ACID transactions (critical for the checkout flow), mature replication for read scaling, InnoDB's row-level locking for concurrent writes, and extensive tooling for backup, monitoring, and performance tuning. |
| **Driver in project** | `mysql-connector-java` version `8.0.32` |
| **Connection** | `jdbc:mysql://127.0.0.1:3306/ecommercedb` |

---

## 5. Security Layer

### Spring Security (via `spring-boot-starter-security`)

| Aspect | Details |
|--------|---------|
| **What it is** | A comprehensive, highly customizable authentication and access-control framework for Java applications. It operates as a servlet filter chain that intercepts every HTTP request before it reaches the application. |
| **Why it is used** | EcommApp configures Spring Security for stateless HTTP Basic authentication. The `SecurityFilterChain` bean defines the authorization policy (registration is public, everything else requires authentication), disables CSRF (appropriate for stateless APIs), and enforces `STATELESS` session management. `UserServiceImpl` implements `UserDetailsService` to load user credentials from MySQL for authentication. |
| **How it helps in real-world systems** | Spring Security is the industry standard for Java application security. It provides pluggable authentication mechanisms (HTTP Basic, form login, OAuth2, SAML, JWT), method-level authorization (`@PreAuthorize`, `@Secured`), CORS configuration, and protection against common attacks. Its filter chain architecture means security is enforced at the framework level, not scattered across business code. |

### BCrypt (via Spring Security)

| Aspect | Details |
|--------|---------|
| **What it is** | An adaptive, one-way password hashing algorithm based on the Blowfish cipher. It includes an automatic per-password salt and a configurable work factor that controls computational cost. |
| **Why it is used** | All user passwords are hashed with `BCryptPasswordEncoder` before storage. During authentication, Spring Security's `DaoAuthenticationProvider` uses the same encoder to compare the submitted password against the stored hash. |
| **How it helps in real-world systems** | BCrypt is intentionally slow (tunable via work factor), making brute-force attacks computationally infeasible. The per-password salt defeats rainbow table attacks. It is recommended by OWASP and used by major platforms for password storage. Alternatives in the same class include Argon2 and scrypt. |

---

## 6. Validation

### Hibernate Validator / Jakarta Bean Validation (via `spring-boot-starter-validation`)

| Aspect | Details |
|--------|---------|
| **What it is** | The reference implementation of the Jakarta Bean Validation specification (JSR 380). It provides declarative constraint annotations (`@NotBlank`, `@Email`, `@Size`, `@Min`, `@NotNull`, `@DecimalMin`) that are enforced automatically when `@Valid` is placed on a controller method parameter. |
| **Why it is used** | Every request DTO in EcommApp is annotated with validation constraints. Invalid input is rejected at the controller boundary with a structured 400 response — before any business logic or database interaction occurs. |
| **How it helps in real-world systems** | Declarative validation centralizes input rules on the DTO itself, making constraints self-documenting and impossible to accidentally bypass. It reduces defensive `if` checks throughout the service layer, keeps error response formatting consistent, and integrates cleanly with Spring MVC's `@RestControllerAdvice` for centralized error handling. |

---

## 7. Developer Productivity

### Project Lombok

| Aspect | Details |
|--------|---------|
| **What it is** | A compile-time annotation processor that generates boilerplate Java code — getters, setters, constructors, `toString()`, `equals()`, `hashCode()`, and builder patterns — from annotations placed on classes. |
| **Why it is used** | All six entities and all eleven DTOs in EcommApp use Lombok annotations (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`, `@Data`, `@ToString`, `@EqualsAndHashCode`). This eliminates hundreds of lines of mechanical code while keeping classes concise and focused on their fields. |
| **How it helps in real-world systems** | Lombok reduces code noise, speeds up development, and prevents errors in handwritten boilerplate (e.g., forgetting to update `equals()`/`hashCode()` after adding a field). The `@Builder` pattern is especially valuable for creating complex objects like `Order` and `Cart` in a readable, immutable-friendly way. Lombok is used in the majority of enterprise Java projects. |
| **Note** | Lombok is declared as `<optional>true</optional>` and excluded from the final JAR by the Maven plugin — it is a compile-time-only tool with zero runtime footprint. |

---

## 8. Build & Dependency Management

### Apache Maven

| Aspect | Details |
|--------|---------|
| **What it is** | A build automation and dependency management tool for Java projects. It uses a declarative `pom.xml` file to define project metadata, dependencies, plugins, and build lifecycle phases. |
| **Why it is used** | EcommApp's `pom.xml` declares all dependencies with compatible versions managed by the `spring-boot-starter-parent` BOM (Bill of Materials). The `spring-boot-maven-plugin` packages the application as an executable fat JAR with all dependencies embedded. The Maven Wrapper (`mvnw`) is included, allowing the project to be built without a global Maven installation. |
| **How it helps in real-world systems** | Maven provides reproducible builds — the same `pom.xml` produces the same artifact on any machine. Its central repository (Maven Central) hosts over 10 million artifacts, making dependency resolution automatic. The BOM-based version management from `spring-boot-starter-parent` prevents dependency conflicts (a.k.a. "JAR hell"). In CI/CD pipelines, `mvn clean package` is typically the single command that compiles, tests, and packages the application. |

### Maven Wrapper (`mvnw` / `mvnw.cmd`)

| Aspect | Details |
|--------|---------|
| **What it is** | A script bundled with the project that downloads and uses a specific Maven version, ensuring all developers and CI servers use the exact same build tool version. |
| **Why it is used** | Eliminates "works on my machine" build issues. A new contributor can clone the repository and run `./mvnw clean package` without installing Maven. |
| **How it helps in real-world systems** | Build reproducibility is critical for regulated industries and large teams. The wrapper ensures CI/CD pipelines, local builds, and production artifact generation all use identical tooling. |

---

## 9. Testing

### Spring Boot Test (via `spring-boot-starter-test`)

| Aspect | Details |
|--------|---------|
| **What it is** | A curated testing dependency that bundles JUnit 5 (Jupiter), Mockito, AssertJ, Hamcrest, JSONPath, and Spring's `MockMvc` and `@SpringBootTest` support into a single starter. |
| **Why it is used** | The project includes test classes (`EcommBackendApplicationTests`, `dummyTests`) that use this framework. `@SpringBootTest` loads the full application context for integration testing, while Mockito enables isolated unit testing of service classes. |
| **How it helps in real-world systems** | Automated testing is non-negotiable for production systems. Spring Boot Test's `MockMvc` can test REST endpoints without starting a real server, `@DataJpaTest` can test repositories against an in-memory database, and `@WebMvcTest` can test controllers in isolation. These sliced tests run in seconds, enabling fast CI feedback loops. |

---

## 10. Transitive / Embedded Technologies

These technologies are not explicitly declared in `pom.xml` but are pulled in transitively by the starters and play significant roles at runtime.

| Technology | Pulled In By | Role in EcommApp |
|------------|-------------|------------------|
| **Apache Tomcat 9.x** | `spring-boot-starter-web` | Embedded HTTP server serving all REST endpoints on port 8080 |
| **HikariCP** | `spring-boot-starter-data-jpa` | High-performance JDBC connection pool managing MySQL connections |
| **SLF4J + Logback** | `spring-boot-starter-web` | Logging facade and implementation; all Spring, Hibernate, and Tomcat logs route through Logback |
| **Jackson Databind** | `spring-boot-starter-web` | JSON ↔ Java object mapping for all request/response bodies |
| **Hibernate ORM 5.6.x** | `spring-boot-starter-data-jpa` | JPA implementation; entity mapping, SQL generation, caching, lazy loading |
| **Spring AOP** | `spring-boot-starter-data-jpa` | Powers `@Transactional` proxy generation for declarative transaction management |
| **Byte Buddy** | Hibernate (transitive) | Runtime bytecode generation for entity proxies (lazy loading) and Spring AOP proxies |
| **JUnit Jupiter 5.x** | `spring-boot-starter-test` | Test execution engine and assertion framework |
| **Mockito 4.x** | `spring-boot-starter-test` | Mocking framework for unit tests |

---

## 11. Adoption-Ready Technologies (Not Yet Configured)

The following technologies are **not present** in the current codebase but the architecture is designed to adopt them with minimal friction.

| Technology | Why It Fits | What Would Be Needed |
|------------|-------------|----------------------|
| **Docker** | The application is a self-contained JAR with an embedded server — the ideal unit for containerization. | Add a `Dockerfile` (3–5 lines: base JRE image, copy JAR, set entrypoint). |
| **Kubernetes** | Stateless authentication and externalized configuration make the app horizontally scalable with zero code changes. | Add K8s manifests (Deployment, Service, ConfigMap/Secret, Ingress). Configure liveness/readiness probes via Spring Actuator. |
| **Spring Boot Actuator** | The starter ecosystem already supports it. | Add `spring-boot-starter-actuator` to `pom.xml` for `/actuator/health`, `/actuator/metrics`, and Kubernetes probe endpoints. |
| **JWT / OAuth2** | `SecurityConfig` is already modular — the `SecurityFilterChain` can be reconfigured to validate JWT tokens instead of Basic credentials. | Add `spring-boot-starter-oauth2-resource-server`, replace `httpBasic()` with `oauth2ResourceServer().jwt()`. |
| **Flyway / Liquibase** | Replace `ddl-auto=update` with versioned, repeatable database migrations for production safety. | Add `flyway-core` dependency and SQL migration scripts in `db/migration/`. |
| **Redis** | HikariCP already manages connection pooling; Redis would add distributed caching and session storage (if sessions are introduced). | Add `spring-boot-starter-data-redis` and configure cache annotations. |
| **Kafka / RabbitMQ** | Event-driven patterns (e.g., "order placed" → notification, inventory sync) are a natural next step. | Add `spring-boot-starter-amqp` or `spring-kafka` and define event producers/consumers. |
| **Spring Cloud Config** | `application.properties` is already externalized; a config server centralizes configuration across multiple service instances. | Add `spring-cloud-starter-config` and point to a Git-backed config repository. |

---

## 12. Technology Stack Summary

```
┌─────────────────────────────────────────────────────────────┐
│                      CLIENT (HTTP)                          │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│  EMBEDDED TOMCAT 9.x  (serves HTTP on port 8080)            │
├─────────────────────────────────────────────────────────────┤
│  SPRING SECURITY  (HTTP Basic, BCrypt, Stateless Sessions)  │
├─────────────────────────────────────────────────────────────┤
│  SPRING WEB MVC  (REST Controllers, Jackson JSON, @Valid)   │
├─────────────────────────────────────────────────────────────┤
│  SERVICE LAYER  (Spring @Transactional, Business Logic)     │
├─────────────────────────────────────────────────────────────┤
│  SPRING DATA JPA  (Repository Interfaces, Query Derivation) │
├─────────────────────────────────────────────────────────────┤
│  HIBERNATE ORM 5.6  (Entity Mapping, SQL Generation, Cache) │
├─────────────────────────────────────────────────────────────┤
│  HikariCP  (JDBC Connection Pooling)                        │
├─────────────────────────────────────────────────────────────┤
│  MySQL Connector/J 8.0.32  (JDBC Driver)                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│  MySQL 8  (ecommercedb — InnoDB, ACID Transactions)         │
└─────────────────────────────────────────────────────────────┘

Build:   Apache Maven + Maven Wrapper
Language: Java 17 (LTS)
DevTools: Lombok (compile-time only)
Testing:  JUnit 5 + Mockito + Spring Test
Logging:  SLF4J + Logback
```
