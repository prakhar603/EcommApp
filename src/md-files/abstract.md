# EcommApp – Project Abstract

## Purpose

EcommApp is a backend e-commerce platform built to handle the core transactional lifecycle of an online store — from user registration and product management to cart operations and order checkout. It provides a RESTful API layer that can serve as the backbone for any consumer-facing storefront or mobile application.

## Key Backend Capabilities

- **User Management** – Registration with encrypted credentials and authenticated profile access.
- **Product Catalog** – Full CRUD operations for managing a product inventory including stock tracking.
- **Shopping Cart** – Per-user persistent carts with support for adding, updating, and removing items, along with real-time stock validation.
- **Order Processing** – Atomic checkout flow that validates inventory, creates orders with line items, deducts stock, and clears the cart in a single transaction.
- **Security** – Stateless HTTP Basic authentication powered by Spring Security with BCrypt password hashing. All endpoints except registration are protected.
- **Validation & Error Handling** – Centralized exception handling with structured error responses (400, 404, 500) and DTO-level input validation.

## Technologies Used

- **Language & Framework** – Java 17, Spring Boot 2.7
- **Data Layer** – Spring Data JPA, Hibernate, MySQL 8
- **Security** – Spring Security (stateless, HTTP Basic, BCrypt)
- **Build Tool** – Maven
- **Other** – Lombok, Bean Validation (Jakarta)

## Real-World Application

EcommApp models a production-grade backend for small-to-medium online retailers. Its layered architecture (Controller → Service → Repository) follows industry-standard separation of concerns, making it straightforward to extend with features like payment gateway integration, shipping logistics, or a front-end SPA. The stateless security model and clean REST API design also make it well-suited for containerized deployment behind an API gateway in a microservices ecosystem.
