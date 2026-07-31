package com.accounting.service;

import com.accounting.dto.BonusPointsResultDto;
import com.accounting.entity.BonusPointsAudit;
import com.accounting.entity.BonusPointsHistory;
import com.accounting.entity.Spending;
import com.accounting.entity.User;
import com.accounting.repository.BonusPointsAuditRepository;
import com.accounting.repository.BonusPointsHistoryRepository;
import com.accounting.repository.SpendingRepository;
import com.accounting.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BonusPointsService
 * Focus on the core reward logic
 */
@DisplayName("BonusPointsService Tests")
@ExtendWith(MockitoExtension.class)
class BonusPointsServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SpendingRepository spendingRepository;
    
    @Mock
    private BonusPointsHistoryRepository bonusPointsHistoryRepository;
    
    @Mock
    private BonusPointsAuditRepository bonusPointsAuditRepository;
    
    @InjectMocks
    private BonusPointsService bonusPointsService;
    
    private User testUser;
    private Spending carChargingSpending;
    private Spending grocerySpending;
    
    @BeforeEach
    void setup() {
        testUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .bonusPoints(0)
            .build();
        
        carChargingSpending = Spending.builder()
            .id(100L)
            .userId(1L)
            .category("Car Charging")
            .amount(new BigDecimal("10.00"))
            .transactionDate(LocalDate.now())
            .build();
        
        grocerySpending = Spending.builder()
            .id(101L)
            .userId(1L)
            .category("Groceries")
            .amount(new BigDecimal("50.00"))
            .transactionDate(LocalDate.now())
            .build();
    }
    
    // ============= HAPPY PATH TESTS =============
    
    @Test
    @DisplayName("Test 1: Single car charging awards 1 point")
    void testAwardPointsForCarCharging_SingleTransaction() {
        // Given
        testUser.setBonusPoints(0);
        
        // When
        BonusPointsResultDto result = bonusPointsService.awardPoints(testUser, carChargingSpending);
        
        // Then
        assertThat(result.getPreviousPoints()).isZero();
        assertThat(result.getPointsAwarded()).isEqualTo(1);
        assertThat(result.getNewPointsBalance()).isEqualTo(1);
        assertThat(result.getRewardTriggered()).isFalse();
        assertThat(testUser.getBonusPoints()).isEqualTo(1);
        
        // Verify audit was created
        verify(bonusPointsAuditRepository, times(1)).save(any(BonusPointsAudit.class));
    }
    
    @Test
    @DisplayName("Test 2: Accumulation - multiple transactions")
    void testAccumulatePoints_FourTransactions() {
        // Given
        testUser.setBonusPoints(0);
        
        // When - log 4 transactions
        bonusPointsService.awardPoints(testUser, carChargingSpending);
        bonusPointsService.awardPoints(testUser, carChargingSpending);
        bonusPointsService.awardPoints(testUser, carChargingSpending);
        bonusPointsService.awardPoints(testUser, carChargingSpending);
        
        // Then
        assertThat(testUser.getBonusPoints()).isEqualTo(4);
        verify(bonusPointsAuditRepository, times(4)).save(any(BonusPointsAudit.class));
    }
    
    // ============= CRITICAL TESTS =============
    
    @Test
    @DisplayName("Test 3: CRITICAL - Reward triggered at exactly 15 points")
    void testRewardTriggered_At15Points() {
        // Given
        testUser.setBonusPoints(14);
        
        BonusPointsHistory reward = BonusPointsHistory.builder()
            .id(1L)
            .userId(1L)
            .pointsEarned(15)
            .rewardCategory("Car Charging")
            .build();
        
        when(bonusPointsHistoryRepository.save(any(BonusPointsHistory.class)))
            .thenReturn(reward);
        
        // When
        BonusPointsResultDto result = bonusPointsService.awardPoints(testUser, carChargingSpending);
        
        // Then
        assertThat(result.getPreviousPoints()).isEqualTo(14);
        assertThat(result.getNewPointsBalance()).isEqualTo(0);  // RESET to 0
        assertThat(result.getRewardTriggered()).isTrue();
        assertThat(result.getRewardDetails()).isNotNull();
        assertThat(result.getRewardDetails().getPointsEarned()).isEqualTo(15);
        
        // Verify reward was saved
        verify(bonusPointsHistoryRepository, times(1)).save(any(BonusPointsHistory.class));
    }
    
    @Test
    @DisplayName("Test 4: CRITICAL - Multiple rewards in sequence")
    void testMultipleRewards_TwoSequentialCycles() {
        // Given
        testUser.setBonusPoints(0);
        
        BonusPointsHistory reward1 = BonusPointsHistory.builder()
            .id(1L)
            .userId(1L)
            .pointsEarned(15)
            .rewardCategory("Car Charging")
            .build();
        
        BonusPointsHistory reward2 = BonusPointsHistory.builder()
            .id(2L)
            .userId(1L)
            .pointsEarned(15)
            .rewardCategory("Car Charging")
            .build();
        
        when(bonusPointsHistoryRepository.save(any(BonusPointsHistory.class)))
            .thenReturn(reward1)
            .thenReturn(reward2);
        
        // When - First cycle (15 points)
        testUser.setBonusPoints(0);
        for (int i = 0; i < 15; i++) {
            bonusPointsService.awardPoints(testUser, carChargingSpending);
        }
        
        assertThat(testUser.getBonusPoints()).isEqualTo(0);  // Reset after first reward
        
        // Second cycle (15 points again)
        for (int i = 0; i < 15; i++) {
            bonusPointsService.awardPoints(testUser, carChargingSpending);
        }
        
        // Then
        assertThat(testUser.getBonusPoints()).isEqualTo(0);  // Reset after second reward
        verify(bonusPointsHistoryRepository, times(2)).save(any(BonusPointsHistory.class));
    }
    
    @Test
    @DisplayName("Test 5: No points for non-charging categories")
    void testNoPoints_ForGroceriesCategory() {
        // Given
        testUser.setBonusPoints(0);
        
        // When
        BonusPointsResultDto result = bonusPointsService.awardPoints(testUser, grocerySpending);
        
        // Then
        assertThat(result.getPointsAwarded()).isEqualTo(0);
        assertThat(result.getNewPointsBalance()).isEqualTo(0);
        assertThat(result.getRewardTriggered()).isFalse();
    }
    
    // ============= EDGE CASE TESTS =============
    
    @Test
    @DisplayName("Test 6: Delete transaction - Points reversed")
    void testPointsReversed_OnTransactionDelete() {
        // Given
        testUser.setBonusPoints(5);
        
        // When
        bonusPointsService.reversePoints(testUser, carChargingSpending);
        
        // Then
        assertThat(testUser.getBonusPoints()).isEqualTo(4);
        
        // Verify audit was created with REVERSED action
        verify(bonusPointsAuditRepository, times(1)).save(any(BonusPointsAudit.class));
    }
    
    @Test
    @DisplayName("Test 7: Delete transaction after reward - Points stay at 0")
    void testDeleteTransactionAfterRewardClaimed_PointsStayReset() {
        // Given - User just claimed reward (points at 0)
        testUser.setBonusPoints(0);
        
        // When - Delete a previous car charging transaction
        bonusPointsService.reversePoints(testUser, carChargingSpending);
        
        // Then - Points should not go negative
        assertThat(testUser.getBonusPoints()).isEqualTo(0);  // NOT -1
    }
    
    @Test
    @DisplayName("Test 8: Amount doesn't affect points for car charging")
    void testPointsAwardedRegardlessOfAmount_CarCharging() {
        // Given
        testUser.setBonusPoints(0);
        
        Spending smallCharge = Spending.builder()
            .id(100L)
            .userId(1L)
            .category("Car Charging")
            .amount(new BigDecimal("1.00"))
            .build();
        
        Spending largeCharge = Spending.builder()
            .id(101L)
            .userId(1L)
            .category("Car Charging")
            .amount(new BigDecimal("100.00"))
            .build();
        
        // When
        BonusPointsResultDto result1 = bonusPointsService.awardPoints(testUser, smallCharge);
        BonusPointsResultDto result2 = bonusPointsService.awardPoints(testUser, largeCharge);
        
        // Then - Both award 1 point each
        assertThat(result1.getPointsAwarded()).isEqualTo(1);
        assertThat(result2.getPointsAwarded()).isEqualTo(1);
    }
    
    // ============= ISOLATION TESTS =============
    
    @Test
    @DisplayName("Test 9: User isolation - Points don't cross users")
    void testPointsIsolated_MultiUser() {
        // Given
        User user1 = User.builder().id(1L).bonusPoints(0).build();
        User user2 = User.builder().id(2L).bonusPoints(0).build();
        
        Spending spending1 = Spending.builder()
            .id(100L)
            .userId(1L)
            .category("Car Charging")
            .amount(new BigDecimal("10.00"))
            .build();
        
        Spending spending2 = Spending.builder()
            .id(101L)
            .userId(2L)
            .category("Car Charging")
            .amount(new BigDecimal("10.00"))
            .build();
        
        // When
        bonusPointsService.awardPoints(user1, spending1);
        for (int i = 0; i < 9; i++) {
            bonusPointsService.awardPoints(user2, spending2);
        }
        
        // Then
        assertThat(user1.getBonusPoints()).isEqualTo(1);
        assertThat(user2.getBonusPoints()).isEqualTo(9);
    }
}
