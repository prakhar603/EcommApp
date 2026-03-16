# EcommApp - E-Commerce Backend API

A production-grade REST API backend for an e-commerce platform built with Spring Boot, Spring Security, and Spring Data JPA.

## Tech Stack

- **Java 17**
- **Spring Boot 2.7.8**
- **Spring Security** (Basic Authentication + BCrypt)
- **Spring Data JPA** (Hibernate ORM)
- **MySQL 8**
- **Lombok**
- **Maven**

## Architecture

Clean layered architecture following SOLID principles:

```
Controller → Service Interface → Service Impl → Repository → Entity → Database
```

### Package Structure

```
com.prakhar.ecomm.ecommbackend
├── config/          # Security configuration
├── controllers/     # REST controllers
├── dto/             # Request & response DTOs
├── entity/          # JPA entities
├── exception/       # Custom exceptions & global handler
├── repository/      # Spring Data JPA repositories
├── service/         # Service interfaces
└── service/impl/    # Service implementations
```

## Modules

### 1. User Module
- User registration with input validation
- BCrypt password encryption
- Profile retrieval for authenticated users

### 2. Product Module
- Full CRUD operations for products
- Input validation on create/update

### 3. Cart Module
- Per-user shopping cart (auto-created on registration)
- Add, remove, and update cart items
- Stock availability validation
- Quantity merging when adding an existing product

### 4. Order Module
- Checkout converts cart into a persisted order
- Stock deduction on successful checkout
- Cart cleared after order creation
- Order summary with itemized pricing and totals

## Database Schema

```
Users ──── 1:1 ──── Cart ──── 1:N ──── CartItem ──── N:1 ──── Products
  │
  └─── 1:N ──── Order ──── 1:N ──── OrderItem ──── N:1 ──── Products
```

| Entity    | Table        | Key Columns                                      |
|-----------|--------------|--------------------------------------------------|
| Users     | `users`      | id, email (unique), first_name, password, age, gender |
| Products  | `products`   | id, productID (unique), name, quantity, price, description |
| Cart      | `cart`       | id, user_id (FK, unique)                         |
| CartItem  | `cart_item`  | id, cart_id (FK), product_id (FK), quantity       |
| Order     | `orders`     | id, user_id (FK), total_amount, created_at        |
| OrderItem | `order_item` | id, order_id (FK), product_id (FK), quantity, price |

## API Endpoints

### User Endpoints

| Method | Endpoint           | Auth     | Description            |
|--------|--------------------|----------|------------------------|
| POST   | `/users/register`  | Public   | Register a new user    |
| GET    | `/users/profile`   | Required | Get current user profile |

### Product Endpoints

| Method | Endpoint          | Auth     | Description         |
|--------|-------------------|----------|---------------------|
| POST   | `/products`       | Required | Create a product    |
| GET    | `/products`       | Required | List all products   |
| GET    | `/products/{id}`  | Required | Get product by ID   |
| PUT    | `/products/{id}`  | Required | Update a product    |
| DELETE | `/products/{id}`  | Required | Delete a product    |

### Cart Endpoints

| Method | Endpoint        | Auth     | Description              |
|--------|-----------------|----------|--------------------------|
| GET    | `/cart`         | Required | View current user's cart |
| POST   | `/cart/add`     | Required | Add product to cart      |
| PUT    | `/cart/update`  | Required | Update cart item quantity |
| DELETE | `/cart/remove`  | Required | Remove item from cart    |

### Order Endpoints

| Method | Endpoint           | Auth     | Description                        |
|--------|--------------------|----------|------------------------------------|
| POST   | `/orders/checkout` | Required | Checkout cart and create order     |

## Security

- **Authentication**: HTTP Basic Authentication
- **Password Storage**: BCrypt hashing via `BCryptPasswordEncoder`
- **Public Endpoint**: Only `POST /users/register` is unauthenticated
- **Session Policy**: Stateless (no server-side sessions)

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Database Setup

```sql
CREATE DATABASE ecommercedb;
```

### Configuration

Update `src/main/resources/application.properties` with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/ecommercedb
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Hibernate will auto-create/update tables on startup (`ddl-auto=update`).

### Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

### Example Requests

**Register a user:**
```bash
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","firstName":"John","password":"secret123","age":28,"gender":"Male"}'
```

**Get profile (Basic Auth):**
```bash
curl -u john@example.com:secret123 http://localhost:8080/users/profile
```

**Create a product:**
```bash
curl -u john@example.com:secret123 -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":999.99,"quantity":50,"description":"Gaming laptop"}'
```

**Add to cart:**
```bash
curl -u john@example.com:secret123 -X POST http://localhost:8080/cart/add \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'
```

**Checkout:**
```bash
curl -u john@example.com:secret123 -X POST http://localhost:8080/orders/checkout
```

## Project Roadmap

- [ ] Add role-based access control (ADMIN / CUSTOMER)
- [ ] Add pagination and sorting for product listing
- [ ] Add order history endpoint (`GET /orders`)
- [ ] Integrate payment gateway (Stripe / Razorpay)
- [ ] Add product search and filtering
- [ ] Add product image upload support
- [ ] Add email notifications on order placement
- [ ] Dockerize the application
- [ ] Add Swagger / OpenAPI documentation
- [ ] Add unit and integration tests
- [ ] Add CI/CD pipeline (GitHub Actions)
- [ ] Add rate limiting and API throttling
- [ ] Add caching layer (Redis) for product catalog

## Error Handling

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-03-16T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient stock. Available: 5"
}
```

Validation errors include field-level details:

```json
{
  "timestamp": "2026-03-16T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "email": "Invalid email format",
    "password": "Password must be at least 6 characters"
  }
}
```

## License

This project is for educational and portfolio purposes.
