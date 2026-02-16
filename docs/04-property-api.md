# Documentation: Property Marketplace API

This document describes the property management endpoints and their features.

## 1. Overview
The Property API allows users to manage and browse real estate listings. Every operation is automatically scoped to the user's tenant.

## 2. Endpoints

### List Properties
- **URL**: `GET /api/v1/properties`
- **Authentication**: Required (JWT)
- **Parameters**:
    - `category` (optional): Filter properties by category (e.g., "Apartment", "Villa").
    - `page` (default: 0): Page number.
    - `size` (default: 10): Number of items per page.
- **Data Isolation**: Only returns properties belonging to the authenticated user's `tenantId`.

### Create Property
- **URL**: `POST /api/v1/properties`
- **Authentication**: Required (JWT)
- **Request Body**: `PropertyDTO`
- **Validation**: Ensures `title`, `price` (positive), and `location` are provided.
- **Data Isolation**: Automatically assigns the new property to the authenticated user's `tenantId`.

## 3. Data Model
- `id`: Unique identifier (generated).
- `title`: Short descriptive name.
- `description`: Detailed information.
- `price`: BigDecimal value.
- `location`: Physical or geographic location.
- `category`: Classification of the property.
- `tenantId`: Automatically handled discriminator.

## 4. Example Request (cURL)
```bash
curl -X POST http://localhost:8080/api/v1/properties \
  -H "Authorization: Bearer <your_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Luxury Penthouse",
    "description": "Amazing view over the city centre",
    "price": 500000,
    "location": "New York, NY",
    "category": "Apartment"
  }'
```
