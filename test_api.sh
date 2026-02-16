#!/bin/bash

BASE_URL="http://localhost:8080/api/v1"

# Logic:
# 1. Onboard Company (Creates Tenant + Admin User)
# 2. Login as Admin
# 3. Admin registers a regular User
# 4. Admin creates a Property
# 5. List properties (tenant isolation check)

RAND=$RANDOM
COMPANY="Company_$RAND"
SLUG="slug_$RAND"
ADMIN_USER="admin_$RAND"
STAFF_USER="staff_$RAND"

echo "1. Onboarding new company: $COMPANY ($SLUG)..."
ONBOARD_RES=$(curl -s -X POST $BASE_URL/onboard \
  -H "Content-Type: application/json" \
  -d "{
    \"companyName\": \"$COMPANY\",
    \"slug\": \"$SLUG\",
    \"adminUsername\": \"$ADMIN_USER\",
    \"adminEmail\": \"$ADMIN_USER@example.com\",
    \"adminPassword\": \"password123\"
  }")
echo "Response: $ONBOARD_RES"

echo -e "\n2. Logging in as Admin: $ADMIN_USER..."
LOGIN_RES=$(curl -s -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$ADMIN_USER\",
    \"password\": \"password123\"
  }")

TOKEN=$(echo $LOGIN_RES | jq -r '.token')
if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
    echo "Login failed!"
    echo "Response: $LOGIN_RES"
    exit 1
fi
echo "Login successful. Token retrieved."

echo -e "\n3. Admin registering a staff user: $STAFF_USER..."
REG_RES=$(curl -s -X POST $BASE_URL/auth/register \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$STAFF_USER\",
    \"email\": \"$STAFF_USER@example.com\",
    \"password\": \"password123\",
    \"roles\": [\"user\"]
  }")
echo "Response: $REG_RES"

echo -e "\n4. Creating a property (as Admin)..."
PROP_RES=$(curl -s -X POST $BASE_URL/properties \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "SaaS Test Property",
    "description": "Created via onboarding flow",
    "price": 500000.00,
    "location": "New York, USA",
    "categoryName": "Commercial"
  }')
echo "Response: $PROP_RES"

echo -e "\n5. Fetching all properties..."
GET_RES=$(curl -s -X GET $BASE_URL/properties \
  -H "Authorization: Bearer $TOKEN")
echo "Response: $GET_RES"

echo -e "\n--- Test Complete ---"
