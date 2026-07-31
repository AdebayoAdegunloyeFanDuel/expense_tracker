#!/bin/bash
# API Test Script for Expense Tracker

BASE_URL="http://localhost:8080/api"

echo "======================================"
echo "Expense Tracker API Test Script"
echo "======================================"
echo ""

# Test 1: Check if API is running
echo "1. Testing root endpoint (GET /)..."
curl -s "$BASE_URL/" | jq . || echo "API not running or jq not installed"
echo ""
echo ""

# Test 2: Register a new user
echo "2. Registering a new user..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "TestPass123"
  }')

echo "$REGISTER_RESPONSE" | jq . 2>/dev/null || echo "$REGISTER_RESPONSE"

# Extract token
TOKEN=$(echo "$REGISTER_RESPONSE" | jq -r '.token' 2>/dev/null)

if [ "$TOKEN" != "null" ] && [ ! -z "$TOKEN" ]; then
    echo ""
    echo "✓ Registration successful! Token obtained."
    echo "Token: $TOKEN"
    echo ""
    echo ""

    # Test 3: Get user profile
    echo "3. Getting user profile..."
    curl -s "$BASE_URL/users/me" \
      -H "Authorization: Bearer $TOKEN" | jq . 2>/dev/null || echo "Failed to get profile"
    echo ""
    echo ""

    # Test 4: Create a spending transaction
    echo "4. Creating a Car Charging transaction..."
    curl -s -X POST "$BASE_URL/spending" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "category": "Car Charging",
        "amount": 25.50,
        "transactionDate": "2024-01-15",
        "notes": "Charged at home"
      }' | jq . 2>/dev/null || echo "Failed to create spending"
    echo ""
    echo ""

    # Test 5: Get dashboard
    echo "5. Getting dashboard..."
    curl -s "$BASE_URL/dashboard" \
      -H "Authorization: Bearer $TOKEN" | jq . 2>/dev/null || echo "Failed to get dashboard"
    echo ""
    echo ""

    echo "======================================"
    echo "Your JWT Token (save this!):"
    echo "$TOKEN"
    echo "======================================"
else
    echo ""
    echo "✗ Registration failed. Check if the API is running on port 8080"
    echo ""
fi

echo ""
echo "Note: If 'jq' is not installed, raw JSON will be displayed."
echo "Install jq with: brew install jq"

