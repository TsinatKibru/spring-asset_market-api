# Documentation: Project Initialization

This document outlines the initial setup of the Secure Multi-tenant Property Marketplace API.

## 1. Project Structure
The project follows a standard Maven directory structure with a clean layered architecture.

```text
src/main/java/com/assetmarket/api/
├── config/           # Infrastructure and security configurations
├── controller/       # REST API endpoints (v1 prefix)
├── dto/              # Data Transfer Objects
├── entity/           # JPA Entities
├── exception/        # Global exception handling
├── repository/       # Spring Data JPA repositories
├── security/         # JWT and security components
└── service/          # Business logic
```

## 2. Dependencies (pom.xml)
Key dependencies used in this project:
- **Spring Boot Starter Web**: For RESTful APIs.
- **Spring Boot Starter Security**: For authentication and authorization.
- **Spring Boot Starter Data JPA**: For database interactions.
- **PostgreSQL Driver**: To connect to the PostgreSQL database.
- **Lombok**: To reduce boilerplate code (Getters, Setters, etc.).
- **JSR-303 Validation**: For input validation.
- **jjwt (JSON Web Token)**: For stateless authentication.
- **SpringDoc OpenAPI (Swagger)**: For API documentation.

## 3. Configuration Profiles
The application uses profile-based configuration:
- `application-dev.yml`: Local development settings using H2 (optional) or a local Postgres.
- `application-prod.yml`: Production-ready settings using environment variables for credentials.

## 4. Initialization Steps
1. Create `pom.xml` with the above dependencies.
2. Define the base package structure.
3. Configure PostgreSQL connection strings in `application.yml`.
4. Set up the Global Exception Handler to ensure consistent error responses.
