#!/bin/bash
# Simple API Test - Correct Format Examples

echo "======================================"
echo "Expense Tracker API - Correct Format"
echo "======================================"
echo ""

# Step 1: Register (or login if already registered)
echo "Step 1: Register a new user..."
echo ""
echo "Command:"
echo 'curl -X POST http://localhost:8080/api/auth/register \'
echo '  -H "Content-Type: application/json" \'
echo '  -d '"'"'{"email":"demo@example.com","password":"Demo123456"}'"'"
echo ""

RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"Demo123456"}')

echo "Response:"
echo "$RESPONSE" | jq . 2>/dev/null || echo "$RESPONSE"
echo ""

# Extract token
TOKEN=$(echo "$RESPONSE" | jq -r '.token' 2>/dev/null)

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
    echo "Registration failed (user might already exist). Trying login..."
    echo ""

    RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"email":"demo@example.com","password":"Demo123456"}')

    TOKEN=$(echo "$RESPONSE" | jq -r '.token' 2>/dev/null)
    echo "Login Response:"
    echo "$RESPONSE" | jq . 2>/dev/null || echo "$RESPONSE"
    echo ""
fi

if [ "$TOKEN" != "null" ] && [ ! -z "$TOKEN" ]; then
    echo "======================================"
    echo "✓ Authentication successful!"
    echo "======================================"
    echo ""
    echo "Your JWT Token:"
    echo "$TOKEN"
    echo ""
    echo "======================================"
    echo ""

    # Step 2: Test Dashboard
    echo "Step 2: Getting Dashboard..."
    echo ""
    echo "Command:"
    echo "curl http://localhost:8080/api/dashboard \\"
    echo "  -H \"Authorization: Bearer YOUR_TOKEN\""
    echo ""

    DASHBOARD=$(curl -s http://localhost:8080/api/dashboard \
      -H "Authorization: Bearer $TOKEN")

    echo "Response:"
    echo "$DASHBOARD" | jq . 2>/dev/null || echo "$DASHBOARD"
    echo ""
    echo "======================================"
    echo ""

    # Step 3: Create a spending
    echo "Step 3: Creating a Car Charging transaction..."
    echo ""
    echo "Command:"
    echo 'curl -X POST http://localhost:8080/api/spending \'
    echo '  -H "Authorization: Bearer YOUR_TOKEN" \'
    echo '  -H "Content-Type: application/json" \'
    echo '  -d '"'"'{"category":"Car Charging","amount":25.50,"transactionDate":"2024-01-15","notes":"Test"}'"'"
    echo ""

    SPENDING=$(curl -s -X POST http://localhost:8080/api/spending \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"category":"Car Charging","amount":25.50,"transactionDate":"2024-01-15","notes":"Test transaction"}')

    echo "Response:"
    echo "$SPENDING" | jq . 2>/dev/null || echo "$SPENDING"
    echo ""
    echo "======================================"
    echo ""

    # Step 4: Get updated dashboard
    echo "Step 4: Getting updated Dashboard (with bonus points)..."
    echo ""

    DASHBOARD2=$(curl -s http://localhost:8080/api/dashboard \
      -H "Authorization: Bearer $TOKEN")

    echo "Response:"
    echo "$DASHBOARD2" | jq . 2>/dev/null || echo "$DASHBOARD2"
    echo ""
    echo "======================================"
    echo ""

    echo "✓ All tests completed!"
    echo ""
    echo "IMPORTANT: The Authorization header format is:"
    echo "  -H \"Authorization: Bearer YOUR_TOKEN\""
    echo ""
    echo "NOT:"
    echo "  -H \"Authorization: YOUR_TOKEN\"  ❌ (missing Bearer prefix)"
    echo ""

else
    echo "❌ Failed to get authentication token"
    echo "Make sure the API is running: mvn spring-boot:run"
fi

