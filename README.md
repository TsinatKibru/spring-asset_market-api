# Secure Multi-tenant Property Marketplace API

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.2-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-blue)
![JWT](https://img.shields.io/badge/JWT-jjwt-orange)
![Tests](https://img.shields.io/badge/Tests-20%20Passing-success)

A production-grade REST API for a property marketplace, featuring robust multi-tenancy isolation, dynamic metadata system, and comprehensive security.

## 🚀 Key Features

### Core Functionality
- **Property Management**: Full CRUD operations for properties with dynamic status and images
- **Category System**: Flexible category management with JSON schema validation
- **Dynamic Metadata**: JSONB-based attribute system with runtime validation
- **Advanced Search**: High-performance filtering by price, location, status, and dynamic attributes
- **Favorites System**: User-specific saved properties with tenant isolation
- **Messaging & Inquiries**: Secure property-linked communication between buyers and merchants

### Security & Multi-tenancy
- **Stateless JWT Security**: Secure authentication and role-based authorization (USER, ADMIN)
- **Multi-tenancy Isolation**: Shared database with discriminator column using Hibernate Filters
- **Tenant-Scoped Data**: All entities automatically filtered by tenant context
- **SaaS Onboarding**: Self-service tenant registration with automatic admin creation

### Architecture & Quality
- **Clean Architecture**: Strictly layered separation (Controller → Service → Repository)
- **Comprehensive Testing**: 20+ integration tests covering all critical paths
- **API Documentation**: Interactive Swagger/OpenAPI documentation
- **Production Ready**: Pagination, versioning, DTO validation, and error handling

## 🛠 Tech Stack

- **Framework**: Spring Boot 3.2.2
- **Database**: PostgreSQL with JSONB support
- **Security**: Spring Security + JJWT
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Testing**: JUnit 5, MockMvc, AssertJ
- **Build Tool**: Maven

## 📋 API Endpoints

### Authentication & Onboarding
- `POST /api/v1/onboarding` - Register new tenant with admin user
- `POST /api/v1/auth/login` - Login (requires `X-Tenant-ID` header)
- `POST /api/v1/auth/register` - Register new user (admin only)

### Categories
- `GET /api/v1/categories` - List all categories
- `POST /api/v1/categories` - Create category (admin only)
- `PUT /api/v1/categories/{id}` - Update category schema (admin only)
- `DELETE /api/v1/categories/{id}` - Delete category (admin only)

### Properties
- `PUT /api/v1/properties/{id}` - Update property (admin only)
- `POST /api/v1/properties/{id}/images` - Upload images (admin only) ✨ **NEW**
- `DELETE /api/v1/properties/{id}` - Delete property (admin only)

### Favorites & Messaging ✨ **NEW**
- `POST /api/v1/favorites/{id}` - Toggle favorite status
- `GET /api/v1/favorites` - List user's saved properties
- `POST /api/v1/messages/inquiry` - Send property inquiry
- `GET /api/v1/messages/thread/{id}` - View conversation thread

## 🏃 Getting Started

### Prerequisites
- **JDK 17** or higher
- **PostgreSQL 12+** (with JSONB support)
- **Maven 3.6+**

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd asset_market
   ```

2. **Configure database**
   
   Set environment variables:
   ```bash
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=asset_market
   export DB_USER=postgres
   export DB_PASSWORD=yourpassword
   ```
   
   Or edit `src/main/resources/application.yml` directly.

3. **Run the application**
   ```bash
   source /etc/environment  # Load JAVA_HOME
   mvn spring-boot:run
   ```

4. **Access Swagger UI**
   
   Open your browser: `http://localhost:8080/swagger-ui.html`

### First Steps

1. **Create a tenant** (via `/api/v1/onboarding`)
2. **Login** with the admin credentials (via `/api/v1/auth/login`)
3. **Create categories** with attribute schemas
4. **Add properties** with validated attributes

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for detailed examples.

## 🧪 Testing

The project includes comprehensive integration tests:

### Test Coverage
- **PropertyUpdateIntegrationTest**: 9 tests for property updates
- **FavoriteIntegrationTest**: Scoped favorites and tenant isolation
- **MessageIntegrationTest**: End-to-end inquiry flow and security
- **ImageUploadIntegrationTest**: Multi-tenant image management
- **CategoryUpdateIntegrationTest**: Category schema updates
- **MetadataValidationIntegrationTest**: Dynamic attribute validation
- **PropertyRepositoryIsolationTest**: Multi-tenant isolation
- **AuthControllerIntegrationTest**: Authentication flow
- **PublicAccessIntegrationTest**: Public property viewing
- And more...

### Run Tests
```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=PropertyUpdateIntegrationTest

# With coverage
mvn test jacoco:report
```

**Current Status**: ✅ 20/20 tests passing

## 📖 Documentation

### Technical Guides
Detailed documentation is available in the `/docs` folder:

1. [Project Initialization](docs/01-initialization.md)
2. [Security & Identity](docs/02-security.md)
3. [Multi-tenancy Isolation](docs/03-multitenancy.md)
4. [Property Marketplace API](docs/04-property-api.md)
5. [Setup and Execution Guide](docs/05-setup-and-execution.md)

### API Documentation
- [Complete API Documentation](API_DOCUMENTATION.md) - Request/response examples
- [Swagger UI](http://localhost:8080/swagger-ui.html) - Interactive API explorer
- [OpenAPI Spec](http://localhost:8080/v3/api-docs) - Machine-readable spec

## 🎯 Key Concepts

### Dynamic Metadata System

Properties can have flexible attributes defined by their category:

```json
{
  "name": "Residential",
  "attributeSchema": [
    {"name": "bedrooms", "type": "number", "required": true},
    {"name": "bathrooms", "type": "number", "required": true},
    {"name": "hasGarage", "type": "boolean", "required": false}
  ]
}
```

Properties are validated against their category schema:

```json
{
  "title": "Modern Family Home",
  "categoryName": "Residential",
  "attributes": {
    "bedrooms": 3,
    "bathrooms": 2,
    "hasGarage": true
  }
}
```

### Multi-Tenancy

All data is automatically scoped to the current tenant:
- **Header-based**: `X-Tenant-ID` header for public requests
- **JWT-based**: Tenant extracted from authenticated user's token
- **Automatic filtering**: Hibernate filters ensure data isolation

## 🔒 Security

### Authentication
- JWT tokens with configurable expiration
- Tenant-scoped user authentication
- Secure password hashing with BCrypt

### Authorization
- Role-based access control (RBAC)
- `ROLE_USER`: Read-only access
- `ROLE_ADMIN`: Full CRUD operations

### Data Isolation
- Tenant context enforced at filter level
- Cross-tenant access attempts return 404
- Comprehensive security tests

## 🚀 Production Deployment

### Environment Variables
```bash
# Database
DB_HOST=your-db-host
DB_PORT=5432
DB_NAME=asset_market_prod
DB_USER=app_user
DB_PASSWORD=secure_password

# JWT
JWT_SECRET=your-256-bit-secret
JWT_EXPIRATION_MS=86400000

# Application
SPRING_PROFILES_ACTIVE=prod
```

### Docker Support
```bash
# Start PostgreSQL
docker-compose up -d

# Build and run application
mvn clean package
java -jar target/api-0.0.1-SNAPSHOT.jar
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Write tests for your changes
4. Commit your changes (`git commit -m 'Add amazing feature'`)
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

Built with:
- Spring Boot
- PostgreSQL
- Hibernate
- Spring Security
- Swagger/OpenAPI
