# Expense Tracker - Spring Boot Project Setup Guide

## Overview
A multi-user accounting system with category-based bonus points rewards built with Spring Boot 3.2, Spring Security, JWT authentication, and JPA.

**Key Features:**
- ✅ User registration & login with JWT tokens
- ✅ Income and spending transaction tracking
- ✅ Bonus points system (1 point per car charging, reward at 15 points)
- ✅ Multi-user data isolation
- ✅ Real-time point calculations
- ✅ Audit trail for debugging

---

## Quick Start (5 minutes)

### 1. Prerequisites
```bash
# Check Java version (need 17+)
java -version

# Check Maven (need 3.8+)
mvn -version
```

### 2. Create Project Structure
```bash
# Create base directory
mkdir expense-tracker
cd expense-tracker

# Create src structure
mkdir -p src/main/java/com/accounting/{entity,dto,repository,service,controller,config,security}
mkdir -p src/main/resources
mkdir -p src/test/java/com/accounting/{service,controller}
```

### 3. Copy Files
Copy all `.java` files into appropriate `src/main/java` folders:
- `entities.java` → `src/main/java/com/accounting/entity/`--  Done
- `dtos.java` → `src/main/java/com/accounting/dto/`-- done
- `repositories.java` → `src/main/java/com/accounting/repository/`-- done
- `services.java` → `src/main/java/com/accounting/service/`-- done
- `controllers.java` → `src/main/java/com/accounting/controller/`-- done
- `security_config.java` → `src/main/java/com/accounting/security/` and `src/main/java/com/accounting/config/`
- `ExpenseTrackerApplication.java` → `src/main/java/com/accounting/`

Copy test file:
- `BonusPointsServiceTest.java` → `src/test/java/com/accounting/service/`

### 4. Copy Configuration Files
- `pom.xml` → project root
- `application.yml` → `src/main/resources/`

### 5. Build and Run
```bash
# Install dependencies and compile
mvn clean install

# Run the application
mvn spring-boot:run

# Output should show:
# Started ExpenseTrackerApplication in X.XXX seconds
```

### 6. Verify Installation
```bash
# Test register endpoint
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"TestPass123"}'

# You should get a response with JWT token
```

---

## File Organization

```
expense-tracker/
├── pom.xml                          # Maven dependencies
├── src/
│   ├── main/
│   │   ├── java/com/accounting/
│   │   │   ├── ExpenseTrackerApplication.java
│   │   │   │
│   │   │   ├── entity/              # JPA entities (database models)
│   │   │   │   ├── User.java
│   │   │   │   ├── Spending.java
│   │   │   │   ├── Income.java
│   │   │   │   ├── BonusPointsHistory.java
│   │   │   │   └── BonusPointsAudit.java
│   │   │   │
│   │   │   ├── dto/                 # Data Transfer Objects (API contracts)
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── CreateSpendingRequest.java
│   │   │   │   ├── UserDto.java
│   │   │   │   ├── SpendingDto.java
│   │   │   │   ├── BonusPointsResultDto.java
│   │   │   │   ├── DashboardDto.java
│   │   │   │   └── ErrorResponse.java
│   │   │   │
│   │   │   ├── repository/          # JPA repositories (data access)
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── SpendingRepository.java
│   │   │   │   ├── IncomeRepository.java
│   │   │   │   ├── BonusPointsHistoryRepository.java
│   │   │   │   └── BonusPointsAuditRepository.java
│   │   │   │
│   │   │   ├── service/             # Business logic (★ CRITICAL)
│   │   │   │   ├── BonusPointsService.java     ← START HERE
│   │   │   │   ├── UserService.java
│   │   │   │   ├── SpendingService.java
│   │   │   │   └── IncomeService.java
│   │   │   │
│   │   │   ├── controller/          # REST endpoints
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── SpendingController.java
│   │   │   │   ├── IncomeController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │
│   │   │   ├── security/            # Security & JWT
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   │
│   │   │   └── config/              # Configuration
│   │   │       └── SecurityConfig.java
│   │   │
│   │   └── resources/
│   │       └── application.yml      # Database, JWT, server config
│   │
│   └── test/
│       └── java/com/accounting/
│           └── service/
│               └── BonusPointsServiceTest.java  ← START WITH THIS
```

---

## Key Concepts

### 1. Entities (Database Models)
Each entity maps to a database table:
- **User**: User account with email, password, bonus points balance
- **Spending**: Individual spending transactions (linked to categories)
- **Income**: Individual income transactions
- **BonusPointsHistory**: Records when rewards are claimed (audit trail)
- **BonusPointsAudit**: Detailed log of every point change (for debugging)

### 2. DTOs (Data Transfer Objects)
DTOs are what the API sends/receives — they don't expose sensitive data:
- **RegisterRequest**: Email + password
- **LoginRequest**: Email + password
- **CreateSpendingRequest**: Amount, category, date, notes
- **UserDto**: Safe-to-expose user info (no password)
- **SpendingDto**: Transaction details

### 3. Repositories (Data Access)
Spring Data JPA repositories handle all database queries:
```java
// Example: Get all spending for a user
List<Spending> findByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable);

// Example: Calculate total spending
BigDecimal getTotalSpendingByUserId(Long userId);
```

### 4. Services (Business Logic)
Services contain the core logic:
- **BonusPointsService** ⭐ — Awards points, triggers rewards, reverses points
- **UserService** — User registration, login, profile
- **SpendingService** — Create/delete spending, calculate totals
- **IncomeService** — Create/delete income, calculate totals

### 5. Controllers (REST Endpoints)
Controllers expose services as HTTP endpoints:
- **AuthController**: `/api/auth/register`, `/api/auth/login`
- **SpendingController**: `/api/spending` (POST, GET, DELETE)
- **IncomeController**: `/api/income` (POST, GET, DELETE)
- **DashboardController**: `/api/dashboard` (GET)
- **UserController**: `/api/users/me` (GET profile)

### 6. Security (JWT Authentication)
- User logs in → receives JWT token
- Token is passed in `Authorization: Bearer {token}` header
- Filter validates token on every request
- User ID is extracted and used to isolate data

---

## Core Business Logic: Bonus Points

**The Rule:**
- 1 point per "Car Charging" transaction
- At 15 points → claim reward → reset to 0
- All other categories → 0 points

**Flow:**
```
User logs spending
    ↓
Check category
    ↓
Is "Car Charging"? → YES → Award 1 point
    ↓                      ↓
    NO → 0 points      Points now = N
                           ↓
                       Is N >= 15? → YES → Create reward, reset to 0
                           ↓                    ↓
                           NO → Done        Audit + notify
```

**Test Cases:**
See `bonus_points_test_cases.md` for 24 comprehensive test scenarios.

---

## API Endpoints

### Authentication

#### Register
```bash
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123"
}

Response 201:
{
  "token": "eyJhbGc...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "bonusPoints": 0,
    "totalIncome": "0.00",
    "totalSpending": "0.00",
    "netBalance": "0.00"
  }
}
```

#### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123"
}

Response 200: (same as register)
```

### Spending

#### Create Spending
```bash
POST /api/spending
Authorization: Bearer {token}
Content-Type: application/json

{
  "category": "Car Charging",
  "amount": 15.50,
  "transactionDate": "2024-01-15",
  "notes": "Charged at home"
}

Response 201:
{
  "id": 100,
  "userId": 1,
  "category": "Car Charging",
  "amount": "15.50",
  "transactionDate": "2024-01-15",
  "notes": "Charged at home",
  "createdAt": "2024-01-15T10:30:00"
}
```

#### Get Spending
```bash
GET /api/spending
Authorization: Bearer {token}

Response 200:
{
  "content": [...list of spending...],
  "page": 1,
  "pageSize": 50,
  "totalElements": 15,
  "totalPages": 1,
  "isLast": true
}
```

#### Delete Spending
```bash
DELETE /api/spending/{spendingId}
Authorization: Bearer {token}

Response 204: (no content)
```

### Dashboard

#### Get Dashboard
```bash
GET /api/dashboard
Authorization: Bearer {token}

Response 200:
{
  "user": {...},
  "totalIncome": "5000.00",
  "totalSpending": "1234.50",
  "netBalance": "3765.50",
  "currentBonusPoints": 7,
  "pointsToNextReward": 8,
  "totalRewardsClaimed": 3,
  "recentSpendings": [...],
  "recentIncomes": [...],
  "recentRewards": [...]
}
```

### User

#### Get Profile
```bash
GET /api/users/me
Authorization: Bearer {token}

Response 200:
{
  "id": 1,
  "email": "user@example.com",
  "bonusPoints": 7,
  "totalIncome": "5000.00",
  "totalSpending": "1234.50",
  "netBalance": "3765.50",
  "createdAt": "2024-01-01T12:00:00"
}
```

---

## Testing Strategy

### Unit Tests (Test Business Logic)
Focus on `BonusPointsServiceTest.java` — test the core logic in isolation:

```bash
# Run all bonus points tests
mvn test -Dtest=BonusPointsServiceTest

# Run specific test
mvn test -Dtest=BonusPointsServiceTest#testRewardTriggered_At15Points
```

**Priority Tests (do these first):**
1. Test 3: Reward trigger at 15 points
2. Test 8: Multiple rewards in sequence
3. Test 11: Delete transaction reverses points
4. Test 18: Concurrent transactions

### Integration Tests (Test with Database)
Test with real database (H2 in-memory):

```bash
mvn test -Dtest=BonusPointsIntegrationTest
```

### Manual Testing
Use curl or Postman to test endpoints end-to-end.

---

## Configuration

### `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb        # H2 in-memory DB (dev/test)
  jpa:
    hibernate.ddl-auto: create-drop # Auto-create tables
    show-sql: false

jwt:
  secret: your-secret-key-min-32-chars-change-in-production!
  expiration: 86400000              # 24 hours in ms

server:
  port: 8080
  servlet:
    context-path: /api
```

### For Production
Update before deploying:
- `jwt.secret` → Use strong, unique secret (32+ chars)
- `spring.datasource.url` → PostgreSQL instead of H2
- `spring.jpa.hibernate.ddl-auto` → Change to `validate`
- `logging.level` → Change to `INFO`

---

## Common Commands

```bash
# Build without running tests
mvn clean package -DskipTests

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Run all tests with detailed output
mvn test -X

# Package as executable jar
mvn clean package

# Run the jar
java -jar target/expense-tracker-1.0.0.jar
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `Cannot find symbol` | Run `mvn clean compile` or check Java version (need 17+) |
| Port 8080 in use | Change port in `application.yml`: `server.port: 8081` |
| JWT secret too short | Update `jwt.secret` in `application.yml` to 32+ chars |
| Tests fail with Mockito error | Ensure `@ExtendWith(MockitoExtension.class)` on test class |
| Database not initializing | Check `spring.jpa.hibernate.ddl-auto` is `create-drop` or `create` |
| Slow dashboard queries | Add indexes (already in entities) and implement pagination |

---

## Next Steps

### This Weekend
- ✅ Copy all files to your project
- ✅ Run `mvn clean install`
- ✅ Run tests: `mvn test`
- ✅ Start app: `mvn spring-boot:run`
- ✅ Test register → login → create spending → view dashboard
- ✅ Manually test bonus points (log 15 car charging transactions)

### Next Week
- Add pagination to transaction lists
- Add filtering by date/category
- Add transaction editing
- Add income tracking
- Add UI (React or just use Postman)

### Long-Term
- Switch to PostgreSQL for production
- Add password reset functionality
- Add email notifications for rewards
- Add monthly/weekly summaries
- Performance testing with 1000+ users
- API documentation (Swagger/OpenAPI)

---

## Learning Path

**If you're learning Spring Boot, focus on:**

1. **Entities & Repositories** — How Spring Data JPA works
   - Look at `entity/` and `repository/` packages
   - Understand how `@Entity`, `@Repository`, derived queries work

2. **Services** — Business logic layer
   - Look at `BonusPointsService.java` (core logic)
   - Understand `@Transactional` and transaction management
   - See how services call repositories

3. **Controllers** — REST endpoints
   - Look at `SpendingController.java`
   - Understand `@RestController`, `@PostMapping`, `@GetMapping`
   - Learn how to validate input and return proper HTTP responses

4. **Security** — JWT authentication
   - Look at `security/` package
   - Understand how `JwtTokenProvider` generates/validates tokens
   - See how `JwtAuthenticationFilter` intercepts requests

5. **Testing** — Unit & integration tests
   - Look at `BonusPointsServiceTest.java`
   - Understand `@Mock`, `@InjectMocks`, `Mockito`
   - Learn how to test business logic in isolation

---

## Questions?

If something doesn't work:
1. Check the error message carefully
2. Search Google for the error
3. Verify Java version: `java -version` (need 17+)
4. Try: `mvn clean install` (sometimes fixes weird issues)
5. Check that all files are in correct directories
6. Make sure `application.yml` is in `src/main/resources/`

Good luck with your project! 🚀
