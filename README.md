# Secure Multi-tenant Property Marketplace API

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.2-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-blue)
![JWT](https://img.shields.io/badge/JWT-jjwt-orange)

A production-grade, interview-ready REST API for a property marketplace, featuring robust multi-tenancy isolation and JWT security.

## 🚀 Key Features
- **Stateless JWT Security**: Secure authentication and role-based authorization.
- **Multi-tenancy Isolation**: Shared database with discriminator column approach using Hibernate Filters.
- **Clean Architecture**: Strictly layered separation (Controller -> Service -> Repository).
- **Automated Isolation**: Aspect-Oriented Programming (AOP) ensures data isolation is transparent to the developer.
- **API Documentation**: Interactive Swagger/OpenAPI documentation.
- **Production Mindset**: Pagination, Versioning, and DTO Validation included.

## 🛠 Tech Stack
- **Framework**: Spring Boot 3.2.2
- **Database**: PostgreSQL
- **Security**: Spring Security + JJWT
- **Documentation**: SpringDoc OpenAPI
- **Testing**: JUnit 5, MockMvc, AssertJ

## 📖 Documentation
Detailed technical guides are available in the `/docs` folder:
1. [Project Initialization](docs/01-initialization.md)
2. [Security & Identity](docs/02-security.md)
3. [Multi-tenancy Isolation](docs/03-multitenancy.md)
4. [Property Marketplace API](docs/04-property-api.md)
5. [Setup and Execution Guide](docs/05-setup-and-execution.md)

## 🏃 Getting Started

### Prerequisites
- JDK 17
- PostgreSQL

### Run the Application
1. Configure database credentials in `src/main/resources/application.yml` or set environment variables:
   ```bash
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=asset_market
   export DB_USER=postgres
   export DB_PASSWORD=yourpassword
   ```
2. Build and run:
   ```bash
   mvn spring-boot:run
   ```

### API Access
Access the interactive documentation (Swagger UI) at:
`http://localhost:8080/swagger-ui.html`

## 🧪 Verification
The project includes critical integration tests:
- `PropertyRepositoryIsolationTest`: Verifies Hibernate filters prevent cross-tenant data leaks.
- `AuthControllerIntegrationTest`: Validates the full register/login flow.

Run tests:
```bash
mvn test
```
