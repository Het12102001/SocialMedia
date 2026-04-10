# SocialEngine: Enterprise-Grade Social Media Backend

**SocialEngine** is a high-performance, secure, and scalable RESTful API built with **Spring Boot 3**. This project demonstrates a deep understanding of complex database relationships, stateless security architectures, and industrial-standard logging and documentation.

---

## Interactive API Documentation (Swagger)
This project is fully documented using **OpenAPI 3.0**. You can explore, test, and interact with all endpoints directly through the Swagger UI once the application is running.

* **Swagger UI:** `http://localhost:8080/swagger-ui.html`
* **API Docs:** `http://localhost:8080/v3/api-docs`

---

##  Killer Features & Functionality

###  Advanced Security Suite
* **JWT Stateless Auth:** Implemented custom `JwtFilter` for secure, scalable session management.
* **Rate Limiting:** Integrated **Bucket4j** to protect against brute-force attacks on sensitive endpoints (Login/Forgot Password).
* **BCrypt Encryption:** Industry-standard password hashing to ensure user data remains secure even in a leak.
* **Bulletproof Recovery:** A multi-step password reset system using UUID tokens with automated old-token cleanup.

### Sophisticated Data Engine
* **Personalized Feed:** Optimized SQL Join queries that filter posts based on a user's unique "Social Graph" (following list).
* **Trending Hashtags:** A high-performance **Native SQL Algorithm** that extracts and ranks hashtags from post content in real-time.
* **Atomic Social Actions:** Follow/Unfollow and Like/Unlike logic with built-in validation (e.g., users cannot follow themselves).
* **Nested Content:** Support for posts with multi-media URLs and full threading for user comments.

### Administrative "God Mode"
* **RBAC (Role-Based Access Control):** Granular permission system where only users with `ROLE_ADMIN` can perform global deletions of posts, comments, or user accounts.

---

## 🛠️ Tech Stack & Infrastructure

| Layer | Technology |
| :--- | :--- |
| **Framework** | Spring Boot 3.4.x |
| **Security** | Spring Security 6 (JWT, Rate Limiting) |
| **Database** | PostgreSQL 15 |
| **ORM** | Spring Data JPA (Hibernate) |
| **Docs** | Swagger UI / SpringDoc OpenAPI |
| **DevOps** | Docker, Docker Compose |
| **Tooling** | Lombok, SLF4J (Logging), Maven |

---

## 🚀 Deployment Guide

### 1. Prerequisites
* Docker & Docker Compose installed.

### 2. Launching the System
```bash
# Clone the repo
git clone [https://github.com/your-username/SocialEngine.git](https://github.com/your-username/SocialEngine.git)

# Spin up the Database and Application containers
docker-compose up -d --build

docker logs -f [app_container_name]
