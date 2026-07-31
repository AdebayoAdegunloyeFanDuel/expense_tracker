# Expense Tracker API

A multi-user accounting REST API built with Spring Boot. Users can log income and spending transactions and earn bonus points for Car Charging transactions — a reward is triggered every 15 points.

---

## Prerequisites

| Tool | Required Version |
|------|-----------------|
| Java (JDK) | 17 |
| Maven | 3.8+ |

> **Important — Java version:** The project must be built and run with **JDK 17**. Maven will use whichever JDK is on your `PATH` by default; if that is Java 21+ you will need to set `JAVA_HOME` explicitly (see [Build](#build) below).

---

## Quick Start

### 1. Clone / open the project

```bash
cd /path/to/IncomeTracker
```

### 2. Build

```bash
# If your default java is already 17:
mvn clean install

# If your default java is 21 or 24 (e.g. Homebrew on macOS):
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  mvn clean install
```

### 3. Run

```bash
# Option A — Maven plugin
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  mvn spring-boot:run

# Option B — JAR (after building)
java -jar target/expense-tracker-1.0.0.jar
```

The API starts on **http://localhost:8080/api**

---

## Configuration

All configuration lives in `src/main/resources/application.yml`.

| Property | Default | Notes |
|----------|---------|-------|
| `server.port` | `8080` | Change if port is in use |
| `spring.datasource.url` | H2 in-memory | Use PostgreSQL URL for production |
| `jwt.secret` | (see file) | **Change before deploying** — must be 32+ chars |
| `jwt.expiration` | `86400000` | Token TTL in milliseconds (24 hours) |

### Switching to PostgreSQL

Replace the datasource block in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/expense_tracker
    username: your_db_user
    password: your_db_password
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
```

---

## API Reference

All endpoints are prefixed with `/api`.

### Authentication

No token required.

#### Register

```
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass1"
}
```

Password rules: minimum 8 characters, must contain at least one number.

**Response:**
```json
{
  "token": "eyJhbGc...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "bonusPoints": 0,
    "totalIncome": 0,
    "totalSpending": 0,
    "netBalance": 0
  }
}
```

#### Login

```
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass1"
}
```

Returns the same shape as register.

---

### Spending

All spending endpoints require a JWT token in the `Authorization` header:
```
Authorization: Bearer <token>
```

Valid categories: `Car Charging`, `Groceries`, `Utilities`, `Other`

#### Log a transaction

```
POST /api/spending
Authorization: Bearer <token>
Content-Type: application/json

{
  "category": "Car Charging",
  "amount": 15.00,
  "transactionDate": "2024-01-15",
  "notes": "Charged at home"
}
```

**Response:** `201 Created` with the created `SpendingDto`.

Logging a `Car Charging` transaction automatically awards 1 bonus point. When the balance reaches 15 points, a reward is triggered and the counter resets to 0.

#### List transactions

```
GET /api/spending
Authorization: Bearer <token>
```

#### Delete a transaction

```
DELETE /api/spending/{id}
Authorization: Bearer <token>
```

Deleting a `Car Charging` transaction reverses the point that was awarded for it.

---

### Dashboard

```
GET /api/dashboard
Authorization: Bearer <token>
```

Returns a summary including total income, total spending, net balance, current bonus points, points until next reward, and recent reward history.

---

### User Profile

```
GET /api/users/me
Authorization: Bearer <token>
```

---

## Bonus Points Rules

| Rule | Detail |
|------|--------|
| Points awarded | 1 point per `Car Charging` transaction |
| Points for other categories | 0 |
| Reward threshold | 15 points |
| After reward | Counter resets to 0 |
| Delete transaction | Point is reversed (balance cannot go below 0) |
| Already-claimed rewards | Not reversed on delete |

---

## H2 Console (development only)

While running locally, browse the in-memory database at:

```
http://localhost:8080/api/h2-console
```

Use these connection settings:

| Field | Value |
|-------|-------|
| JDBC URL | `jdbc:h2:mem:testdb` |
| User Name | `sa` |
| Password | *(leave blank)* |

---

## Running Tests

```bash
# All tests (requires network on first run to download surefire plugin)
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  mvn test

# Specific test class
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  mvn test -Dtest=BonusPointsServiceTest
```

---

## Project Structure

```
src/
├── main/
│   ├── java/com/accounting/
│   │   ├── ExpenseTrackerApplication.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── SpendingController.java
│   │   │   ├── DashboardController.java
│   │   │   ├── UserController.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Spending.java
│   │   │   ├── Income.java
│   │   │   ├── BonusPointsHistory.java
│   │   │   └── BonusPointsAudit.java
│   │   ├── repository/           (Spring Data JPA interfaces)
│   │   ├── service/
│   │   │   ├── BonusPointsService.java   ← core reward logic
│   │   │   ├── SpendingService.java
│   │   │   └── UserService.java
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── CustomUserDetailsService.java
│   │   └── dto/                  (request/response objects)
│   └── resources/
│       └── application.yml
└── test/
    └── java/com/accounting/
        └── service/
            └── BonusPointsServiceTest.java
```

---

## Troubleshooting

**`TypeTag :: UNKNOWN` build crash**
Maven is using Java 21+. Set `JAVA_HOME` to JDK 17 as shown in the [Build](#build) section.

**`Port 8080 already in use`**
Either change `server.port` in `application.yml`, or free the port:
```bash
lsof -ti :8080 | xargs kill -9
```

**`JWT secret is too short` at startup**
Update `jwt.secret` in `application.yml` to a string of at least 32 characters.

**401 Unauthorized on all requests**
Include the token from the register/login response: `Authorization: Bearer <token>`
