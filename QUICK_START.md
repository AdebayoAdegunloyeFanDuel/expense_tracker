# Quick Start Guide - Testing the API

## The Problem You Encountered

When you accessed `http://localhost:8080/api/`, you got a **403 Forbidden** error because:
- There was no endpoint mapped to the root path `/`
- Spring Security blocks all unauthenticated requests by default
- Only `/auth/**` endpoints were public

## The Solution

I've added:
1. **ApiInfoController** - A root endpoint that returns API information
2. **Updated SecurityConfig** - Allows public access to the root endpoint
3. **Test script** - Easy way to test all endpoints

---

## How to Test the API

### Option 1: Use the Test Script (Easiest)

```bash
# Stop the current application (Ctrl+C)
# Rebuild and restart
mvn clean spring-boot:run

# In a new terminal, run:
./test-api-simple.sh
```

This will:
- ✓ Check if the API is running
- ✓ Register a new user
- ✓ Create a spending transaction
- ✓ Get your profile and dashboard
- ✓ Show you the JWT token

### Option 2: Manual Testing with curl

#### 1. Check if API is running
```bash
curl http://localhost:8080/api/
```

**Expected Response:**
```json
{
  "name": "Expense Tracker API",
  "version": "1.0.0",
  "status": "running",
  "documentation": { ... }
}
```

#### 2. Register a new user
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "yourname@example.com",
    "password": "SecurePass1"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGc...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "email": "yourname@example.com",
    "bonusPoints": 0,
    "totalIncome": 0,
    "totalSpending": 0,
    "netBalance": 0
  }
}
```

**Save the token!** You'll need it for authenticated requests.

#### 3. Login (if you already registered)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "yourname@example.com",
    "password": "SecurePass1"
  }'
```

#### 4. Create a spending transaction
```bash
# Replace YOUR_TOKEN_HERE with your actual token from step 2
curl -X POST http://localhost:8080/api/spending \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Car Charging",
    "amount": 25.50,
    "transactionDate": "2024-01-15",
    "notes": "Charged at home"
  }'
```

#### 5. Get your dashboard
```bash
curl http://localhost:8080/api/dashboard \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

#### 6. Get your profile
```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## Restart Steps

If the application is currently running:

1. **Stop it**: Press `Ctrl+C` in the terminal running `mvn spring-boot:run`

2. **Rebuild** (to include the new files):
   ```bash
   mvn clean compile
   ```

3. **Restart**:
   ```bash
   mvn spring-boot:run
   ```

4. **Wait** for the startup message:
   ```
   Started ExpenseTrackerApplication in X.XXX seconds
   ```

5. **Test** in a new terminal:
   ```bash
   curl http://localhost:8080/api/
   ```

   Or run the test script:
   ```bash
   ./test-api-simple.sh
   ```

---

## Common Issues

### "Connection refused"
- The application isn't running
- Check: `lsof -ti :8080` (should return a process ID)
- Solution: Start with `mvn spring-boot:run`

### "403 Forbidden" on root
- Old code is still running
- Solution: Stop (Ctrl+C), rebuild (`mvn clean compile`), restart

### "401 Unauthorized" on protected endpoints
- Missing or invalid token
- Solution: Register/login first to get a token

### "Port 8080 already in use"
- Another application is using the port
- Solution: `lsof -ti :8080 | xargs kill -9` then restart

---

## Browser Testing

You **cannot test POST requests** easily in a browser. Use:
- The test script (recommended)
- `curl` commands above
- Postman or Insomnia (API clients)
- Thunder Client (VS Code extension)

You **can** view in browser:
- `http://localhost:8080/api/` - Root endpoint
- `http://localhost:8080/api/h2-console` - H2 database console

---

## Next Steps

1. Stop the current application
2. Rebuild: `mvn clean compile`  
3. Restart: `mvn spring-boot:run`
4. Test: `./test-api.sh` or `curl http://localhost:8080/api/`

Enjoy your Expense Tracker API! 🚀

