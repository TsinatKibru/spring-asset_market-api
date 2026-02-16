# Documentation: Setup and Execution

This guide provides step-by-step instructions to get the Secure Multi-tenant Property Marketplace API up and running.

## 1. Prerequisites
- **Docker & Docker Compose**: For running the PostgreSQL database.
- **JDK 17**: The project requires Java 17 (verified compatibility).
- **Maven 3.6+**: For project build and dependency management.

## 2. Database Setup (Docker)
The project includes a `docker-compose.yml` file to spin up a PostgreSQL instance.

```bash
# Start the PostgreSQL container
docker-compose up -d

# Verify it is running
docker ps | grep asset-market-db
```

## 3. Building the Application
To ensure compatibility with Spring Boot 3.2.2, use Java 17 for the build.

```bash
# Set JAVA_HOME to Java 17
export JAVA_HOME=/usr/lib/jvm/java-1.17.0-openjdk-amd64

# Clean and install dependencies
mvn clean install -DskipTests
```

## 4. Running Automated Tests
The project includes integration tests that verify multi-tenancy isolation and JWT flows.

```bash
# Run tests
mvn test
```

## 5. Running the Application
Once the build is successful and the database is running:

```bash
# Start the Spring Boot application
mvn spring-boot:run
```

## 6. Accessing the API
- **Base URL**: `http://localhost:8080/api/v1`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui.html`
- **Health Check**: `http://localhost:8080/actuator/health` (if actuator is enabled)

## 7. Configuration Notes
- **JWT Secret**: Configurable via `assetmarket.app.jwtSecret` in `application.yml`.
- **Database Credentials**: Managed via environment variables or direct edits in `application.yml`.
