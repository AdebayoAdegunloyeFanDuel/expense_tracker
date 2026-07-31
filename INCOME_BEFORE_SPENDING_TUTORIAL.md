# How to Add Income Before Spending (Tutorial)

## Current Situation

Your question: **"How can you spend without income?"**

The system currently **allows it** (like a credit card), but you might want to **require income first**.

---

## 🎯 Two Approaches

### **Approach 1: Current Design** ✅ (Recommended)
**Allows negative balances** - Tracks debt/credit like real accounting

**Example:**
```bash
# New user, $0 balance
Register → Balance: $0

# Spend before income (ALLOWED)
Spend $100 → Balance: -$100 ❗ (In debt)

# Add income later
Income $200 → Balance: +$100 ✅ (Positive again)
```

### **Approach 2: Enforce Positive Balance** (Optional)
**Requires income before spending** - Cash-only accounting

**Example:**
```bash
# New user, $0 balance
Register → Balance: $0

# Try to spend (BLOCKED)
Spend $100 → ❌ ERROR: "Insufficient balance"

# Add income first
Income $200 → Balance: +$200

# Now can spend
Spend $100 → Balance: +$100 ✅
```

---

## 📝 How to Use CURRENT System (Add Income)

Even though spending without income is **allowed**, here's the **recommended workflow**:

### Step 1: Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Pass123456"}'
```

**Save the token!**

### Step 2: Add Income FIRST
```bash
curl -X POST http://localhost:8080/api/income \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "source": "Salary",
    "amount": 1000.00,
    "transactionDate": "2024-01-15",
    "notes": "Monthly salary"
  }'
```

### Step 3: Check Balance
```bash
curl http://localhost:8080/api/dashboard \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected:**
```json
{
  "totalIncome": 1000.00,
  "totalSpending": 0,
  "netBalance": 1000.00
}
```

### Step 4: Now Spend
```bash
curl -X POST http://localhost:8080/api/spending \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Groceries",
    "amount": 100.00,
    "transactionDate": "2024-01-15"
  }'
```

### Step 5: Verify Balance
```bash
curl http://localhost:8080/api/dashboard \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected:**
```json
{
  "totalIncome": 1000.00,
  "totalSpending": 100.00,
  "netBalance": 900.00
}
```

---

## 🔧 Option: Add Balance Validation

If you want to **PREVENT** spending without income:

### Step 1: Copy the Optional Service

```bash
cp SpendingServiceWithValidation.java.OPTIONAL \
   src/main/java/com/accounting/service/SpendingServiceWithValidation.java
```

### Step 2: Replace Class Name in SpendingService.java

Replace line 20 from:
```java
public class SpendingService {
```

To:
```java
public class SpendingServiceWithValidation {
```

### Step 3: Add IncomeRepository Dependency

Add this field around line 24:
```java
private final IncomeRepository incomeRepository;
```

### Step 4: Add Validation Method

Add this method before `validateCategory`:

```java
private void validateSufficientBalance(Long userId, BigDecimal requestedAmount) {
    BigDecimal totalIncome = incomeRepository.getTotalIncomeByUserId(userId);
    BigDecimal totalSpending = spendingRepository.getTotalSpendingByUserId(userId);
    BigDecimal currentBalance = totalIncome.subtract(totalSpending);
    
    if (currentBalance.compareTo(requestedAmount) < 0) {
        throw new IllegalStateException(String.format(
            "Insufficient balance. Available: $%.2f, Requested: $%.2f",
            currentBalance, requestedAmount
        ));
    }
}
```

### Step 5: Call Validation in createSpending

Add this line after `validateCategory`:

```java
validateSufficientBalance(userId, request.getAmount());
```

### Step 6: Restart Application

```bash
mvn clean compile
mvn spring-boot:run
```

---

## 🧪 Testing Balance Validation

### With Validation DISABLED (Current):

```bash
# Spend without income - WORKS
Spend $100 → Balance: -$100 ✅ (Allowed, negative balance)
```

### With Validation ENABLED (Optional):

```bash
# Spend without income - FAILS
Spend $100 → ❌ ERROR: "Insufficient balance. Available: $0.00"

# Add income first
Income $200 → Balance: $200 ✅

# Now spending works
Spend $100 → Balance: $100 ✅
```

---

## 💡 Recommendation

**Keep the current behavior** (no validation) because:

1. ✅ **More realistic** - Most people use credit cards
2. ✅ **Flexible** - Can track debt and credit
3. ✅ **Full feature set** - Bonus points work even when in debt
4. ✅ **Real accounting** - Matches how businesses track money

The **test script** shows spending first just for **demo purposes**. In real use:
- Users can add income whenever they want
- The system tracks the **net balance** (positive or negative)
- The negative balance is a **useful indicator** of debt

---

## 📊 Complete Example with Income First

Here's a complete workflow showing the **recommended order**:

```bash
#!/bin/bash

# 1. Register
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"proper@example.com","password":"Pass123"}')

TOKEN=$(echo "$RESPONSE" | jq -r '.token')

# 2. Add Income FIRST  
curl -X POST http://localhost:8080/api/income \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"source":"Salary","amount":2000,"transactionDate":"2024-01-01"}'

# 3. Check balance (should be $2000)
curl http://localhost:8080/api/dashboard \
  -H "Authorization: Bearer $TOKEN"

# 4. Add spending (now have money!)
curl -X POST http://localhost:8080/api/spending \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"category":"Car Charging","amount":50,"transactionDate":"2024-01-15"}'

# 5. Final balance (should be $1950)
curl http://localhost:8080/api/dashboard \
  -H "Authorization: Bearer $TOKEN"
```

Save this as `test-with-income.sh` and run it!

---

## 🎯 Summary

| Question | Answer |
|----------|--------|
| Why can I spend without income? | **Design choice** - tracks debt like credit cards |
| Is this wrong? | **No** - it's intentional and useful |
| Should I change it? | **Optional** - current design is better for most use cases |
| How do I add income first? | Use `/api/income` endpoint before spending |

The system is working **as designed**! Negative balances are a **feature**, not a bug. 📊

