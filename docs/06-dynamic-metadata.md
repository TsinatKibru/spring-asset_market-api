# Dynamic Metadata System (JSONB)

This document explains how the Asset Market API handles flexible property attributes using PostgreSQL JSONB.

## 🏗 Architecture
The system uses a **Schema-on-Write** approach:
1.  **Categories** define the "Rules" (Schema).
2.  **Properties** store the "Data" (Attributes).

### Category Schema
Each category can store an `attributeSchema` which is a list of field definitions:
```json
[
  {"name": "rooms", "type": "number", "required": true},
  {"name": "parking", "type": "boolean", "required": false}
]
```

### Property Attributes
Properties store a flat JSON object in the `attributes` column:
```json
{
  "rooms": 3,
  "parking": true
}
```

## 🛡 Validation rules
The `PropertyService` performs rigorous validation before saving:
- **Presence**: All `required` fields in the category schema must be present.
- **Type Safety**: Field values must match the expected type (`number`, `string`, `boolean`).
- **Strict Mode**: Unrecognized fields (not in schema) are rejected to prevent database pollution.

## 🚀 Database optimization (GIN Index)
To ensure high performance when filtering by dynamic attributes, we recommend adding a `GIN` index in production:

```sql
CREATE INDEX idx_property_attributes ON properties USING GIN (attributes);
```

This allows for lightning-fast lookups inside the JSON structure:
```sql
SELECT * FROM properties WHERE attributes @> '{"rooms": 3}';
```

## 🧪 Testing the validation
You can test this by creating a category with a schema and then attempting to create a property with invalid data (missing fields or wrong types).
