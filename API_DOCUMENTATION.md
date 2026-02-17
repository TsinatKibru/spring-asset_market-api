# API Documentation

Complete API reference for the Asset Market API with request/response examples.

## Table of Contents
- [Authentication](#authentication)
- [Onboarding](#onboarding)
- [Categories](#categories)
- [Properties](#properties)
- [Common Patterns](#common-patterns)

---

## Base URL
```
http://localhost:8080/api/v1
```

## Authentication

All authenticated endpoints require:
- **Authorization header**: `Bearer <JWT_TOKEN>`
- **X-Tenant-ID header**: Your tenant slug

### Login

**Endpoint**: `POST /auth/login`

**Headers**:
```
Content-Type: application/json
X-Tenant-ID: your-tenant-slug
```

**Request**:
```json
{
  "username": "admin_user",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin_user",
  "roles": ["ROLE_ADMIN"]
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: acme-corp" \
  -d '{
    "username": "admin_acme",
    "password": "securepass123"
  }'
```

---

## Onboarding

### Register New Tenant

**Endpoint**: `POST /onboarding`

**Headers**:
```
Content-Type: application/json
```

**Request**:
```json
{
  "tenantName": "Acme Corporation",
  "tenantSlug": "acme-corp",
  "adminUsername": "admin_acme",
  "adminEmail": "admin@acme.com",
  "adminPassword": "securepass123"
}
```

**Response** (201 Created):
```json
{
  "message": "Tenant and admin user created successfully",
  "tenantSlug": "acme-corp",
  "adminUsername": "admin_acme"
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/v1/onboarding \
  -H "Content-Type: application/json" \
  -d '{
    "tenantName": "Acme Corporation",
    "tenantSlug": "acme-corp",
    "adminUsername": "admin_acme",
    "adminEmail": "admin@acme.com",
    "adminPassword": "securepass123"
  }'
```

**Notes**:
- Tenant slug must be unique
- Creates tenant and admin user in a single transaction
- Admin user has `ROLE_ADMIN` and `ROLE_USER`

---

## Categories

### List Categories

**Endpoint**: `GET /categories`

**Headers**:
```
Authorization: Bearer <token>
X-Tenant-ID: your-tenant-slug
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Residential",
    "description": "Housing properties",
    "attributeSchema": [
      {
        "name": "bedrooms",
        "type": "number",
        "required": true
      },
      {
        "name": "bathrooms",
        "type": "number",
        "required": true
      },
      {
        "name": "hasGarage",
        "type": "boolean",
        "required": false
      }
    ]
  },
  {
    "id": 2,
    "name": "Commercial",
    "description": "Business properties",
    "attributeSchema": [
      {
        "name": "zoningCode",
        "type": "string",
        "required": true
      },
      {
        "name": "loadingDocks",
        "type": "number",
        "required": false
      }
    ]
  }
]
```

### Create Category

**Endpoint**: `POST /categories` (Admin only)

**Request**:
```json
{
  "name": "Industrial",
  "description": "Warehouses and factories",
  "attributeSchema": [
    {
      "name": "squareFootage",
      "type": "number",
      "required": true
    },
    {
      "name": "ceilingHeight",
      "type": "number",
      "required": false
    },
    {
      "name": "hasRailAccess",
      "type": "boolean",
      "required": false
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "id": 3,
  "name": "Industrial",
  "description": "Warehouses and factories",
  "attributeSchema": [
    {
      "name": "squareFootage",
      "type": "number",
      "required": true
    },
    {
      "name": "ceilingHeight",
      "type": "number",
      "required": false
    },
    {
      "name": "hasRailAccess",
      "type": "boolean",
      "required": false
    }
  ]
}
```

### Update Category

**Endpoint**: `PUT /categories/{id}` (Admin only)

**Request**:
```json
{
  "name": "Residential",
  "description": "Updated description",
  "attributeSchema": [
    {
      "name": "bedrooms",
      "type": "number",
      "required": true
    },
    {
      "name": "bathrooms",
      "type": "number",
      "required": true
    },
    {
      "name": "hasGarage",
      "type": "boolean",
      "required": false
    },
    {
      "name": "energyRating",
      "type": "string",
      "required": false
    }
  ]
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "Residential",
  "description": "Updated description",
  "attributeSchema": [
    {
      "name": "bedrooms",
      "type": "number",
      "required": true
    },
    {
      "name": "bathrooms",
      "type": "number",
      "required": true
    },
    {
      "name": "hasGarage",
      "type": "boolean",
      "required": false
    },
    {
      "name": "energyRating",
      "type": "string",
      "required": false
    }
  ]
}
```

**Notes**:
- Schema is fully replaced (not merged)
- Existing properties retain their old attribute data
- New properties must conform to the new schema

### Delete Category

**Endpoint**: `DELETE /categories/{id}` (Admin only)

**Response** (204 No Content)

**Error** (400 Bad Request):
```json
{
  "message": "Cannot delete category with existing properties",
  "status": 400,
  "error": "Bad Request"
}
```

---

## Properties

### List Properties

**Endpoint**: `GET /properties`

**Query Parameters**:
- `page` (default: 0)
- `size` (default: 20)
- `category` (optional filter)

**Headers** (optional for public access):
```
X-Tenant-ID: your-tenant-slug
```

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "title": "Modern Family Home",
      "description": "Beautiful 3-bedroom house in quiet neighborhood",
      "price": 450000.00,
      "location": "123 Maple Drive, Suburbia",
      "categoryName": "Residential",
      "attributes": {
        "bedrooms": 3,
        "bathrooms": 2,
        "hasGarage": true,
        "energyRating": "A"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

**Example with filtering**:
```bash
curl -X GET "http://localhost:8080/api/v1/properties?category=Residential&page=0&size=10" \
  -H "X-Tenant-ID: acme-corp"
```

### Create Property

**Endpoint**: `POST /properties` (Admin only)

**Request**:
```json
{
  "title": "Luxury Penthouse",
  "description": "Stunning 4-bedroom penthouse with city views",
  "price": 1250000.00,
  "location": "456 Skyline Boulevard, Downtown",
  "categoryName": "Residential",
  "attributes": {
    "bedrooms": 4,
    "bathrooms": 3,
    "hasGarage": true,
    "energyRating": "A+"
  }
}
```

**Response** (201 Created):
```json
{
  "id": 2,
  "title": "Luxury Penthouse",
  "description": "Stunning 4-bedroom penthouse with city views",
  "price": 1250000.00,
  "location": "456 Skyline Boulevard, Downtown",
  "categoryName": "Residential",
  "attributes": {
    "bedrooms": 4,
    "bathrooms": 3,
    "hasGarage": true,
    "energyRating": "A+"
  }
}
```

**Validation Errors** (400 Bad Request):
```json
{
  "message": "Missing required attribute: bedrooms",
  "status": 400,
  "error": "Bad Request"
}
```

### Update Property ✨ NEW

**Endpoint**: `PUT /properties/{id}` (Admin only)

**Request**:
```json
{
  "title": "Updated Luxury Penthouse",
  "description": "Recently renovated with new features",
  "price": 1350000.00,
  "location": "456 Skyline Boulevard, Downtown",
  "categoryName": "Residential",
  "attributes": {
    "bedrooms": 5,
    "bathrooms": 4,
    "hasGarage": true,
    "energyRating": "A++"
  }
}
```

**Response** (200 OK):
```json
{
  "id": 2,
  "title": "Updated Luxury Penthouse",
  "description": "Recently renovated with new features",
  "price": 1350000.00,
  "location": "456 Skyline Boulevard, Downtown",
  "categoryName": "Residential",
  "attributes": {
    "bedrooms": 5,
    "bathrooms": 4,
    "hasGarage": true,
    "energyRating": "A++"
  }
}
```

**Change Category Example**:
```json
{
  "title": "Converted Office Space",
  "description": "Former residential converted to commercial",
  "price": 800000.00,
  "location": "456 Skyline Boulevard, Downtown",
  "categoryName": "Commercial",
  "attributes": {
    "zoningCode": "C-2",
    "loadingDocks": 1
  }
}
```

**Notes**:
- Full replacement of property data
- Can change category (must provide valid attributes for new category)
- Attributes validated against category schema
- Tenant isolation enforced

### Delete Property

**Endpoint**: `DELETE /properties/{id}` (Admin only)

**Response** (204 No Content)

**Error** (404 Not Found):
```json
{
  "message": "Property not found",
  "status": 404,
  "error": "Not Found"
}
```

---

## Common Patterns

### Error Responses

All errors follow this format:

```json
{
  "timestamp": "2026-02-17T10:30:00",
  "message": "Descriptive error message",
  "status": 400,
  "error": "Bad Request"
}
```

**Common Status Codes**:
- `200 OK` - Success
- `201 Created` - Resource created
- `204 No Content` - Success with no response body
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found

### Pagination

All list endpoints support pagination:

```
GET /properties?page=0&size=20
```

**Response includes**:
- `content`: Array of items
- `pageable`: Pagination metadata
- `totalElements`: Total count
- `totalPages`: Total pages

### Tenant Isolation

All requests are automatically scoped to the tenant:
- **Public requests**: Use `X-Tenant-ID` header
- **Authenticated requests**: Tenant extracted from JWT

Cross-tenant access returns `404 Not Found` (not `403`) to prevent information leakage.

### Attribute Validation

Properties are validated against their category's schema:

**Valid Types**:
- `string`
- `number`
- `boolean`

**Required Attributes**:
- Must be present in the attributes object
- Cannot be null

**Optional Attributes**:
- Can be omitted
- Can be null

**Example Schema**:
```json
{
  "name": "bedrooms",
  "type": "number",
  "required": true
}
```

**Valid Attribute**:
```json
{
  "bedrooms": 3
}
```

**Invalid Attributes**:
```json
{
  "bedrooms": "three"  // Wrong type (string instead of number)
}
```

```json
{
  // Missing required attribute "bedrooms"
}
```

---

## Complete Example Workflow

### 1. Register Tenant
```bash
curl -X POST http://localhost:8080/api/v1/onboarding \
  -H "Content-Type: application/json" \
  -d '{
    "tenantName": "Real Estate Co",
    "tenantSlug": "realestate-co",
    "adminUsername": "admin",
    "adminEmail": "admin@realestate.com",
    "adminPassword": "secure123"
  }'
```

### 2. Login
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: realestate-co" \
  -d '{
    "username": "admin",
    "password": "secure123"
  }' | jq -r '.token')
```

### 3. Create Category
```bash
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: realestate-co" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Residential",
    "description": "Housing properties",
    "attributeSchema": [
      {"name": "bedrooms", "type": "number", "required": true},
      {"name": "bathrooms", "type": "number", "required": true}
    ]
  }'
```

### 4. Create Property
```bash
curl -X POST http://localhost:8080/api/v1/properties \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: realestate-co" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Cozy Apartment",
    "description": "Perfect starter home",
    "price": 250000.00,
    "location": "123 Main St",
    "categoryName": "Residential",
    "attributes": {
      "bedrooms": 2,
      "bathrooms": 1
    }
  }'
```

### 5. Update Property
```bash
curl -X PUT http://localhost:8080/api/v1/properties/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: realestate-co" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Renovated Cozy Apartment",
    "description": "Recently updated with modern fixtures",
    "price": 275000.00,
    "location": "123 Main St",
    "categoryName": "Residential",
    "attributes": {
      "bedrooms": 2,
      "bathrooms": 2
    }
  }'
```

### 6. View Properties (Public)
```bash
curl -X GET http://localhost:8080/api/v1/properties \
  -H "X-Tenant-ID: realestate-co"
```

---

## Postman Collection

Import this collection into Postman for easy testing:

1. Create a new collection: "Asset Market API"
2. Add environment variables:
   - `baseUrl`: `http://localhost:8080/api/v1`
   - `tenantId`: Your tenant slug
   - `token`: (will be set automatically after login)
3. Import the requests from this documentation
4. Use `{{baseUrl}}`, `{{tenantId}}`, and `{{token}}` variables

**Pro tip**: Set up a login request that saves the token to the environment:
```javascript
// In the "Tests" tab of the login request
pm.environment.set("token", pm.response.json().token);
```

---

## Additional Resources

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- **Testing Guide**: See `testing_guide.md` in the artifacts directory
