# Dynamic Metadata System (JSONB)

This document explains how the Asset Market API handles flexible property attributes using PostgreSQL JSONB and the advanced filtering capabilities.

## 🏗 Architecture
The system uses a **Schema-on-Write** approach:
1.  **Categories** define the "Rules" (Schema).
2.  **Properties** store the "Data" (Attributes).

### Category Schema
Each category can store an `attributeSchema` which is a list of field definitions:
```json
[
  {"name": "bedrooms", "type": "number", "required": true},
  {"name": "bathrooms", "type": "number", "required": true},
  {"name": "hasGarage", "type": "boolean", "required": false}
]
```

### Property Attributes
Properties store a flat JSON object in the `attributes` column:
```json
{
  "bedrooms": 3,
  "bathrooms": 2,
  "hasGarage": true
}
```

## 🛡 Validation Rules
The `PropertyService` performs rigorous validation during creation and updates:
- **Presence**: All `required` fields in the category schema must be present.
- **Type Safety**: Field values must match the expected type (`number`, `string`, `boolean`).
- **Strict Mode**: Unrecognized fields (not in schema) are rejected to prevent database pollution.

## 🚀 Advanced Filtering
The system supports complex queries on dynamic attributes using Spring Data Type-Safe Specifications and PostgreSQL's `@>` operator.

### API Usage
To filter properties by a dynamic attribute, use the `attrKey` and `attrValue` parameters:
`GET /api/v1/properties?attrKey=bedrooms&attrValue=3`

### Under the Hood
The backend automatically detects the type of the attribute from the category schema and casts the query parameter accordingly (e.g., converting a string "3" to a Long) before performing the JSONB contains operation.

## 📈 Performance
For production environments, we utilize a `GIN` index on the `attributes` column:
```sql
CREATE INDEX idx_property_attributes ON properties USING GIN (attributes);
```
This enables lightning-fast attribute-based search even across millions of listings.
