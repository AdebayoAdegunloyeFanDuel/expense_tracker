# Bonus Points Logic - Comprehensive Test Cases

## Overview
The bonus points system is **mission-critical** for user engagement. One miscalculation = lost trust. These test cases cover unit, integration, and edge cases.

**Core Rule:** 1 point per Car Charging transaction. At 15 points → reward triggered → reset to 0.

---

## UNIT TESTS (Test the Business Logic in Isolation)

### Test Suite: `BonusPointsServiceTest`

#### **1. Happy Path: Single Transaction to Reward**
- **Test Name:** `testAwardPointsForCarCharging_SingleTransaction`
- **Scenario:** User logs 1 car charging → expects 1 point awarded
- **Setup:** Create user with 0 points
- **Action:** Log 1 car charging transaction (amount $10, any date)
- **Assert:**
  - User's point balance = 1
  - No reward triggered (balance < 15)
  - BonusPointsHistory empty
- **Why:** Validates basic point award mechanism

---

#### **2. Accumulation: Multiple Transactions**
- **Test Name:** `testAccumulatePoints_FourTransactions`
- **Scenario:** User logs 4 car charging transactions
- **Setup:** User with 0 points
- **Action:** 
  - Log TX 1 (car charging, $5) → expect 1 point
  - Log TX 2 (car charging, $20) → expect 2 points total
  - Log TX 3 (car charging, $7) → expect 3 points total
  - Log TX 4 (car charging, $15) → expect 4 points total
- **Assert:** Final balance = 4 points, no reward triggered
- **Why:** Validates accumulation across multiple transactions

---

#### **3. CRITICAL: Reward Trigger at Exactly 15 Points**
- **Test Name:** `testRewardTriggered_At15Points`
- **Scenario:** User reaches exactly 15 points
- **Setup:** Create user, manually set points to 14
- **Action:** Log 1 car charging transaction
- **Assert:**
  - User's point balance = 0 (reset)
  - BonusPointsHistory has 1 entry with:
    - `rewardClaimedDate` = today
    - `pointsEarned` = 15
    - `rewardCategory` = "Car Charging"
  - Reward triggered flag = true
- **Why:** This is the core business trigger — it MUST work exactly

---

#### **4. Boundary: 14 Points (No Trigger)**
- **Test Name:** `testNoRewardTriggered_At14Points`
- **Scenario:** User at 14 points logs 1 transaction
- **Setup:** Manually set user to 14 points
- **Action:** Log 1 car charging
- **Assert:**
  - Balance = 15 points
  - Reward NOT triggered
  - BonusPointsHistory empty
- **Why:** Validates boundary — off by one bugs are common

---

#### **5. Boundary: 16 Points (Overshoot)**
- **Test Name:** `testRewardTriggered_Overshoot_OneExtraPoint`
- **Scenario:** User at 14 points, logs transaction, ends at 15+
- **Setup:** User at 14 points
- **Action:** Log 2 car charging (should trigger at 15, reset, then add 1)
- **Assert:**
  - Balance = 1 point (not 16, reset happened)
  - BonusPointsHistory has 1 entry
- **Why:** Validates reset doesn't lose leftover points

---

#### **6. No Points for Non-Charging Categories**
- **Test Name:** `testNoPoints_ForGroceriesCategory`
- **Scenario:** User logs groceries (not car charging)
- **Setup:** User with 0 points
- **Action:** Log spending with category="Groceries", amount=$50
- **Assert:**
  - Point balance = 0
  - No reward triggered
- **Why:** Validates category filtering

---

#### **7. No Points for Zero or Negative Amount**
- **Test Name:** `testNoPoints_ZeroAmount`
- **Scenario:** User logs car charging with $0
- **Setup:** Normal user
- **Action:** Log car charging, amount=0
- **Assert:**
  - Point balance unchanged (still 0)
  - Validation error OR transaction rejected
- **Why:** Prevents gaming the system

---

#### **8. CRITICAL: Multiple Rewards in Sequence**
- **Test Name:** `testMultipleRewards_TwoSequentialCycles`
- **Scenario:** User earns 15 points twice in same session
- **Setup:** User with 0 points
- **Action:** 
  - Log 15 car charging transactions
  - (Points hit 15, reset to 0, reward 1 claimed)
  - Log 15 more car charging transactions
  - (Points hit 15 again, reset to 0, reward 2 claimed)
- **Assert:**
  - Final balance = 0
  - BonusPointsHistory has 2 entries
  - Second reward's `id` > first reward's `id`
  - Both rewards have different `rewardClaimedDate`
- **Why:** Validates system doesn't break on second cycle (state management)

---

#### **9. Transaction Amount Doesn't Affect Points (Car Charging)**
- **Test Name:** `testPointsAwardedRegardlessOfAmount_CarCharging`
- **Scenario:** 1 point for $1 car charging vs $100 car charging
- **Setup:** Two users, both at 0 points
- **Action:** 
  - User A logs car charging: $1
  - User B logs car charging: $100
- **Assert:**
  - User A balance = 1
  - User B balance = 1
- **Why:** Validates business rule: points are per-transaction, not per-dollar

---

#### **10. User Isolation: Points Don't Cross Users**
- **Test Name:** `testPointsIsolated_MultiUser`
- **Scenario:** User A and User B both earn points
- **Setup:** Create 2 users, both at 0 points
- **Action:** 
  - User A logs 5 car charging
  - User B logs 10 car charging
- **Assert:**
  - User A balance = 5
  - User B balance = 10
  - No reward triggered for either
- **Why:** Multi-user data isolation (matches Story 1 risk)

---

#### **11. CRITICAL: Delete Transaction — Points Reversed**
- **Test Name:** `testPointsReversed_OnTransactionDelete`
- **Scenario:** User logs car charging (1 point awarded), then deletes it
- **Setup:** User at 0 points
- **Action:** 
  - Log car charging TX (ID=123) → points = 1
  - Delete transaction 123
- **Assert:**
  - Point balance = 0
  - Transaction 123 no longer in history
  - BonusPointsHistory unchanged (deletion doesn't affect claimed rewards)
- **Why:** Edge case — if you don't handle this, users can delete then re-log for double points

---

#### **12. Delete Transaction After Reward Claimed**
- **Test Name:** `testDeleteTransactionAfterRewardClaimed_PointsStayReset`
- **Scenario:** User logs 15 transactions (triggers reward, resets to 0), then deletes one of those 15
- **Setup:** User with reward already claimed, points at 0
- **Action:** 
  - Log 15 car charging (reward claimed, points = 0)
  - Delete one of those 15 transactions
- **Assert:**
  - Point balance = 0 (NOT -1)
  - Reward still in history
  - Transaction removed from spending history
- **Why:** Prevents negative points or reward reversal

---

#### **13. Same Transaction Logged Twice (Duplicate)**
- **Test Name:** `testNoDuplicatePoints_IfTransactionLoggedTwice`
- **Scenario:** Race condition or user double-clicks "save"
- **Setup:** User at 0 points
- **Action:** 
  - Attempt to log car charging TX twice with idempotency key or same timestamp + amount
- **Assert:**
  - Points = 1 (not 2)
  - Only 1 transaction in history
- **Why:** Prevents accidental double-award

---

#### **14. Points Awarded Instantly (No Delay)**
- **Test Name:** `testPointsAwardedImmediately_NoAsync`
- **Scenario:** User logs transaction and immediately checks balance
- **Setup:** User at 0 points
- **Action:** 
  - Log car charging
  - Retrieve user balance in same request/transaction
- **Assert:**
  - Balance = 1 (not 0, not pending)
- **Why:** Users expect real-time feedback

---

#### **15. Partial Deletion Edge Case (Bulk Delete)**
- **Test Name:** `testBulkDeleteTransactions_PointsReversedCorrectly`
- **Scenario:** User deletes 3 car charging transactions at once
- **Setup:** User with 5 points (5 car charging logged)
- **Action:** 
  - Delete 3 car charging transactions
  - Call point recalculation
- **Assert:**
  - Balance = 2
  - Only 2 transactions remain
- **Why:** Bulk operations can cause subtle bugs

---

---

## INTEGRATION TESTS (Test with Real Database)

### Test Suite: `BonusPointsIntegrationTest`

#### **16. End-to-End: Log Spending → Award Points → View on Dashboard**
- **Test Name:** `testE2E_LogSpending_PointsVisibleOnDashboard`
- **Scenario:** User logs car charging, then fetches dashboard
- **Setup:** Real user in DB, 0 points
- **Action:** 
  1. POST `/api/spending` with car charging
  2. GET `/api/dashboard`
- **Assert:**
  - Dashboard response includes `currentPoints: 1`
  - Latest transaction visible in dashboard
- **Why:** Validates data flows from service → repository → response

---

#### **17. Database Persistence: Points Survive Restart**
- **Test Name:** `testPointsPersist_AfterApplicationRestart`
- **Scenario:** Log points, app restarts, points still there
- **Setup:** Log 5 car charging in real DB
- **Action:** 
  1. Verify points = 5
  2. Clear application cache (if any)
  3. Fetch user points from fresh query
- **Assert:**
  - Points = 5 (not reset)
- **Why:** Validates DB schema and persistence

---

#### **18. Concurrency: Two Transactions Logged Simultaneously**
- **Test Name:** `testConcurrentTransactionLogging_NoRaceCondition`
- **Scenario:** User with 14 points, two transactions logged at same millisecond
- **Setup:** User at 14 points
- **Action:** 
  - Thread A: Log car charging (point 15, should trigger reward)
  - Thread B: Log car charging (should go to 1 point after reset)
  - Both execute in parallel
- **Assert:**
  - One reward triggered
  - Final balance = 1 (or 0, depending on order — but not 2)
  - No data corruption
- **Why:** Prevents double-reward race condition in high-load scenarios

---

#### **19. Database Constraint: Duplicate Point Awards**
- **Test Name:** `testDatabaseConstraint_PreventsDuplicateRewardRecords`
- **Scenario:** Application bug tries to insert same reward twice
- **Setup:** Normal user earning reward
- **Action:** 
  - Attempt to insert BonusPointsHistory entry twice with same details
- **Assert:**
  - Unique constraint prevents second insert
  - Exception thrown cleanly
- **Why:** Database-level safety net for bugs in code

---

#### **20. Transaction Rollback: Spending Not Saved**
- **Test Name:** `testPointsNotAwarded_IfSpendingTransactionRollsBack`
- **Scenario:** User logs spending, but database transaction fails
- **Setup:** User at 0 points
- **Action:** 
  - Log car charging (inject failure in repository save)
  - Transaction should rollback
- **Assert:**
  - Spending NOT saved
  - Points NOT awarded
  - User balance still = 0
- **Why:** Prevents inconsistent state (points awarded but no spending record)

---

---

## EXPLORATORY / MANUAL TESTS

### Test Suite: User-Facing Scenarios

#### **21. UI Validation: User Sees Points Update in Real-Time**
- **Test Name:** Manual browser test
- **Scenario:** Log 15 transactions via UI, watch points update live
- **Setup:** Log in, navigate to dashboard
- **Action:** Click "Add Spending", enter car charging 15 times (manually or script)
- **Assert:**
  - Points displayed increase from 0 → 15
  - When 15 hit, points reset to 0 and reward banner appears
  - User sees "Reward Claimed!" message
- **Why:** UX reality check — does it *feel* responsive?

---

#### **22. Data Accuracy After Bulk Transactions**
- **Test Name:** Manual batch test
- **Scenario:** Load 100 transactions, verify total points
- **Setup:** Script to create 100 transactions
- **Action:** 
  - Generate 100 car charging (various amounts)
  - Calculate expected points: 100 points = 6 rewards + 10 leftover
  - Verify in dashboard
- **Assert:**
  - Points = 10
  - BonusPointsHistory count = 6
- **Why:** Validates accuracy at scale

---

#### **23. Edge Case: Leap Year / DST Date Issues**
- **Test Name:** Manual date test
- **Scenario:** Log transactions on Feb 29, DST boundary
- **Setup:** System date set to leap year date
- **Action:** Log spending on Feb 29
- **Assert:**
  - Transaction saved with correct date
  - No date parsing errors
- **Why:** Time/date bugs are subtle and hard to reproduce

---

#### **24. Network Fault: Points Partially Awarded**
- **Test Name:** Manual network fault injection
- **Scenario:** Network cuts out mid-response (spending saved, points not awarded)
- **Setup:** Use network throttling/proxy
- **Action:** 
  - Log car charging
  - Cut network before response returns
  - Reconnect and refresh
- **Assert:**
  - Spending is in history (saved)
  - Points are awarded (eventual consistency)
  - No "orphaned" spending with 0 points
- **Why:** Real-world resilience test

---

---

## TEST DATA & FIXTURES

### Setup Helper: Create Standard Test Data

```java
// For unit tests
User testUser = User.builder()
    .id(1L)
    .email("test@example.com")
    .bonusPoints(0)
    .build();

Spending carChargingTx = Spending.builder()
    .userId(1L)
    .category("Car Charging")
    .amount(10.00)
    .date(LocalDate.now())
    .build();
```

### Database Reset Between Tests
- Use `@Transactional` on test methods (auto-rollback)
- Or `@DirtiesContext` if you need manual cleanup
- Or TestContainers for true DB isolation

---

## Test Coverage Target

| Area | Target | Rationale |
|------|--------|-----------|
| BonusPointsService | 95%+ | Core business logic — every path matters |
| BonusPointsRepository | 85%+ | DB interactions, mostly covered by integration tests |
| SpendingController | 70%+ | API happy path + error cases |
| SecurityConfig | 60%+ | Hard to unit test, covered by integration tests |

---

## Risk-Based Test Prioritization (Do These First)

🔴 **MUST TEST (Today):**
1. Test 3: Reward trigger at 15 points
2. Test 8: Multiple rewards in sequence
3. Test 11: Delete transaction reverses points
4. Test 18: Concurrent transaction logging
5. Test 21: Real-time UI update

🟠 **SHOULD TEST (This weekend):**
6. Tests 1–7 (happy path + boundaries)
7. Tests 16–17 (end-to-end + persistence)

🟡 **NICE TO TEST (Later):**
8. Tests 13–15, 19–20, 22–24 (edge cases, concurrency, manual)

---

## Example Test Implementation (JUnit 5 + Mockito)

```java
@DisplayName("BonusPointsService Tests")
class BonusPointsServiceTest {

    @Mock
    private SpendingRepository spendingRepository;
    @Mock
    private BonusPointsHistoryRepository historyRepository;
    
    @InjectMocks
    private BonusPointsService bonusPointsService;
    
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test 3: Should trigger reward at exactly 15 points")
    void testRewardTriggeredAt15Points() {
        // Setup
        User user = new User(1L, 14);
        Spending carCharging = new Spending(1L, "Car Charging", 10.00, LocalDate.now());
        
        // Action
        BonusPointsResult result = bonusPointsService.awardPoints(user, carCharging);
        
        // Assert
        assertThat(result.getNewPointBalance()).isEqualTo(0);
        assertThat(result.isRewardTriggered()).isTrue();
        verify(historyRepository, times(1)).save(any(BonusPointsHistory.class));
    }
}
```

---

## Key Takeaways

1. **Bonus points is high-risk** — one off-by-one error breaks the system
2. **Test deletion early** — this cascades into accounting errors
3. **Concurrency matters** — multi-user systems need thread safety
4. **Persistence is critical** — bogus data in DB kills trust
5. **Automate the 95%** you can, then manually explore the weird 5%

Run these tests **before** you touch the UI. Your code will be solid.

---

## Next Steps

1. Copy these test names into your task list (Jira/GitHub Issues)
2. Implement the code → implement the tests (TDD-style, or at least code-first + test-second)
3. Aim to run all **🔴 MUST TEST** by end of Day 1
4. Celebrate when all tests pass 🎉
