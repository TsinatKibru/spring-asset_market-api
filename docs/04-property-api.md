# Documentation: Property Marketplace API

This document describes the property management endpoints and their features.

## 1. Overview
The Property API allows users to manage and browse real estate listings. Every operation is automatically scoped to the user's tenant.

## 2. Endpoints

### List Properties
- **URL**: `GET /api/v1/properties`
- **Authentication**: Optional (JWT for private/merchant lists)
- **Parameters**:
    - `category` (optional): Filter properties by category name.
    - `minPrice / maxPrice` (optional): Price range filtering.
    - `location` (optional): Partial match on location string.
    - `attrKey / attrValue` (optional): Advanced filtering on dynamic attributes.
- **Data Isolation**: Only returns properties belonging to the specified `X-Tenant-ID` or authenticated user's `tenantId`.

### Create Property
- **URL**: `POST /api/v1/properties`
- **Authentication**: Required (Merchant/Admin)
- **Request Body**: `PropertyDTO`
- **Validation**: Ensures `title`, `price` (positive), and `location` are provided. Attributes are validated against the category's JSON schema.

### Image Management
- **Standalone Upload**: `POST /api/v1/properties/upload` - Upload images before property creation.
- **Direct Upload**: `POST /api/v1/properties/{id}/images` - Add images to an existing property.
- **Serving**: Images are served via `/uploads/**` with full Next.js optimization support.

## 3. Data Model
- `id`: Unique identifier (generated).
- `title`: Short descriptive name.
- `description`: Detailed information.
- `price`: BigDecimal value.
- `location`: Physical or geographic location.
- `categoryName`: Unique name of the category.
- `imageUrls`: List of relative paths to uploaded images.
- `attributes`: JSONB map of category-specific dynamic specs.
- `tenantId`: Automatically handled discriminator.

## 4. Example Request (cURL)
```bash
curl -X POST http://localhost:8080/api/v1/properties \
  -H "Authorization: Bearer <your_token>" \
  -H "X-Tenant-ID: default" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Skyline Luxury Office",
    "description": "Premium office space with panoramic views",
    "price": 1200000,
    "location": "Downtown Business District",
    "categoryName": "Commercial",
    "attributes": {
      "floor": 42,
      "sqft": 5000
    },
    "imageUrls": ["/uploads/default/image1.jpg"]
  }'
```
