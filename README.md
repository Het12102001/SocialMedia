# 🚀 Social Media Platform - Backend API

A secure, scalable, and fully containerized REST API for a modern social media platform. Built with a focus on "Zero-Hardcode" security, automated CI/CD, and robust multi-container orchestration.

## 🛠️ Tech Stack

* **Core:** Java 21, Spring Boot 3
* **Database:** PostgreSQL 15
* **Cache:** Redis
* **Security:** JWT (JSON Web Tokens), OTP, Magic Links
* **AI Integration:** Google Gemini Pro API
* **Email:** Mailtrap (SMTP)
* **DevOps:** Docker, Docker Compose, GitHub Actions
* **Hosting:** Render (Cloud Native PaaS)

## ✨ Key Features

* **Advanced Authentication:** Secure login using OTP and Magic Links (Phase 3).
* **Zero-Hardcode Security:** Strict environment variable injection. App fails securely if credentials are missing.
* **Continuous Integration:** Automated GitHub Actions pipeline that compiles Java, runs tests, and verifies Docker builds on every push to `master`.
* **Containerized Infrastructure:** One-click local deployment using Docker Compose for the App, Postgres, and Redis.
* **AI-Powered:** Integrated with Google's Gemini AI model.

---

## 💻 Local Development Setup

### Prerequisites
* Java 21 (JDK)
* Docker & Docker Desktop

### 1. Environment Configuration
Create a `.env` file in the root directory (next to `docker-compose.yml`). **Do not commit this file to version control.**

```env
# 🐘 Database Secrets
DB_USER=postgres
DB_PASSWORD=your_secure_password
DB_NAME=social_media

# 🤖 AI & Security Secrets
GEMINI_API_KEY=your_gemini_api_key
JWT_SECRET=your_super_secret_jwt_key

# 📧 Mailtrap Secrets
MAIL_USER=your_mailtrap_user
MAIL_PASSWORD=your_mailtrap_password
