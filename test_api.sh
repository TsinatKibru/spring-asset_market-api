#!/bin/bash

BASE_URL="http://localhost:8080/api/v1"
USERNAME="user_$(date +%s)"
PASSWORD="password123"
TENANT="tenant_123"

echo "1. Registering user: $USERNAME..."
REG_RES=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"email\": \"$USERNAME@example.com\",
    \"password\": \"$PASSWORD\",
    \"tenantId\": \"$TENANT\",
    \"roles\": [\"admin\"]
  }")
echo "Response: $REG_RES"

echo -e "\n2. Logging in..."
LOGIN_RES=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"password\": \"$PASSWORD\"
  }")

TOKEN=$(echo $LOGIN_RES | grep -oP '(?<="token":")[^"]+')

if [ -z "$TOKEN" ]; then
    echo "Error: Could not retrieve token. Login response: $LOGIN_RES"
    exit 1
fi

echo "Login successful. Token retrieved."

echo -e "\n3. Creating a property..."
PROP_RES=$(curl -s -X POST "$BASE_URL/properties" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Automated Test Property",
    "description": "Created by test_api.sh",
    "price": 1250.50,
    "location": "Paris, France",
    "categoryName": "Apartment"
  }')
echo "Response: $PROP_RES"

echo -e "\n4. Fetching all properties (scoped to tenant)..."
GET_RES=$(curl -s -X GET "$BASE_URL/properties" \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $GET_RES"

echo -e "\n--- Test Complete ---"
