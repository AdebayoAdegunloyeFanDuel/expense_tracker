package com.accounting.service;

import com.accounting.dto.BonusPointsResultDto;
import com.accounting.dto.CreateSpendingRequest;
import com.accounting.dto.SpendingDto;
import com.accounting.entity.Spending;
import com.accounting.entity.User;
import com.accounting.repository.SpendingRepository;
import com.accounting.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SpendingService
 * Focus on spending creation, deletion, and bonus points integration
 */
@DisplayName("SpendingService Tests")
@ExtendWith(MockitoExtension.class)
class SpendingServiceTest {

    @Mock
    private SpendingRepository spendingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BonusPointsService bonusPointsService;

    @InjectMocks
    private SpendingService spendingService;

    private User testUser;
    private Spending carChargingSpending;
    private CreateSpendingRequest spendingRequest;
    private BonusPointsResultDto bonusPointsResult;

    @BeforeEach
    void setup() {
        testUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .bonusPoints(5)
            .build();

        carChargingSpending = Spending.builder()
            .id(100L)
            .userId(1L)
            .category("Car Charging")
            .amount(new BigDecimal("25.00"))
            .transactionDate(LocalDate.now())
            .notes("Tesla charging")
            .build();

        spendingRequest = CreateSpendingRequest.builder()
            .category("Car Charging")
            .amount(new BigDecimal("25.00"))
            .transactionDate(LocalDate.now())
            .notes("Tesla charging")
            .build();

        bonusPointsResult = BonusPointsResultDto.builder()
            .previousPoints(5)
            .pointsAwarded(1)
            .newPointsBalance(6)
            .rewardTriggered(false)
            .build();
    }

    // ============= HAPPY PATH TESTS =============

    @Test
    @DisplayName("Test 1: Create spending successfully")
    void testCreateSpending_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(spendingRepository.save(any(Spending.class))).thenReturn(carChargingSpending);
        when(bonusPointsService.awardPoints(any(User.class), any(Spending.class)))
            .thenReturn(bonusPointsResult);

        // When
        SpendingDto result = spendingService.createSpending(1L, spendingRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getCategory()).isEqualTo("Car Charging");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("25.00"));
        verify(spendingRepository).save(any(Spending.class));
        verify(bonusPointsService).awardPoints(any(User.class), any(Spending.class));
    }

    @Test
    @DisplayName("Test 2: Create spending for groceries")
    void testCreateSpending_Groceries() {
        // Given
        CreateSpendingRequest groceryRequest = CreateSpendingRequest.builder()
            .category("Groceries")
            .amount(new BigDecimal("50.00"))
            .transactionDate(LocalDate.now())
            .build();

        Spending grocerySpending = Spending.builder()
            .id(101L)
            .userId(1L)
            .category("Groceries")
            .amount(new BigDecimal("50.00"))
            .transactionDate(LocalDate.now())
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(spendingRepository.save(any(Spending.class))).thenReturn(grocerySpending);
        when(bonusPointsService.awardPoints(any(User.class), any(Spending.class)))
            .thenReturn(bonusPointsResult);

        // When
        SpendingDto result = spendingService.createSpending(1L, groceryRequest);

        // Then
        assertThat(result.getCategory()).isEqualTo("Groceries");
        verify(bonusPointsService).awardPoints(any(User.class), any(Spending.class));
    }

    // ============= CRITICAL TESTS =============

    @Test
    @DisplayName("Test 3: CRITICAL - Delete spending reverses points")
    void testDeleteSpending_ReversesPoints() {
        // Given
        when(spendingRepository.findById(100L)).thenReturn(Optional.of(carChargingSpending));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(bonusPointsService).reversePoints(any(User.class), any(Spending.class));
        doNothing().when(spendingRepository).deleteByIdAndUserId(100L, 1L);

        // When
        spendingService.deleteSpending(1L, 100L);

        // Then
        verify(bonusPointsService).reversePoints(testUser, carChargingSpending);
        verify(spendingRepository).deleteByIdAndUserId(100L, 1L);
    }

    @Test
    @DisplayName("Test 4: CRITICAL - Cannot delete another user's spending")
    void testDeleteSpending_UnauthorizedAccess() {
        // Given
        when(spendingRepository.findById(100L)).thenReturn(Optional.of(carChargingSpending));

        // When/Then
        assertThatThrownBy(() -> spendingService.deleteSpending(2L, 100L))
            .isInstanceOf(SecurityException.class)
            .hasMessage("User does not own this spending record");

        verify(bonusPointsService, never()).reversePoints(any(), any());
        verify(spendingRepository, never()).deleteByIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Test 5: Create spending - User not found")
    void testCreateSpending_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> spendingService.createSpending(999L, spendingRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User not found");

        verify(spendingRepository, never()).save(any());
    }

    // ============= EDGE CASE TESTS =============

    @Test
    @DisplayName("Test 6: Invalid category throws exception")
    void testCreateSpending_InvalidCategory() {
        // Given
        CreateSpendingRequest invalidRequest = CreateSpendingRequest.builder()
            .category("Invalid Category")
            .amount(new BigDecimal("50.00"))
            .transactionDate(LocalDate.now())
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> spendingService.createSpending(1L, invalidRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid category");

        verify(spendingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 7: Delete spending - Spending not found")
    void testDeleteSpending_NotFound() {
        // Given
        when(spendingRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> spendingService.deleteSpending(1L, 999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Spending not found");
    }

    @Test
    @DisplayName("Test 8: Get spending for user - service method works")
    void testGetSpendingForUser_Success() {
        // Given - Mock Page response
        Page<Spending> page = new PageImpl<>(List.of(carChargingSpending));
        when(spendingRepository.findByUserIdOrderByTransactionDateDesc(eq(1L), any()))
            .thenReturn(page);

        // When
        List<SpendingDto> result = spendingService.getSpendingForUser(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(spendingRepository).findByUserIdOrderByTransactionDateDesc(eq(1L), any());
    }

    @Test
    @DisplayName("Test 9: Get spending calls repository")
    void testGetSpendingForUser_CallsRepository() {
        // Given - Mock empty Page
        Page<Spending> emptyPage = new PageImpl<>(List.of());
        when(spendingRepository.findByUserIdOrderByTransactionDateDesc(eq(1L), any()))
            .thenReturn(emptyPage);

        // When
        spendingService.getSpendingForUser(1L);

        // Then
        verify(spendingRepository).findByUserIdOrderByTransactionDateDesc(eq(1L), any());
    }

    // ============= ISOLATION TESTS =============

    @Test
    @DisplayName("Test 10: Valid categories - All accepted")
    void testCreateSpending_AllValidCategories() {
        // Given
        List<String> validCategories = List.of("Car Charging", "Groceries", "Utilities", "Other");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(spendingRepository.save(any(Spending.class))).thenReturn(carChargingSpending);
        when(bonusPointsService.awardPoints(any(User.class), any(Spending.class)))
            .thenReturn(bonusPointsResult);

        // When/Then
        for (String category : validCategories) {
            CreateSpendingRequest request = CreateSpendingRequest.builder()
                .category(category)
                .amount(new BigDecimal("10.00"))
                .transactionDate(LocalDate.now())
                .build();

            assertThatCode(() -> spendingService.createSpending(1L, request))
                .doesNotThrowAnyException();
        }

        verify(spendingRepository, times(4)).save(any(Spending.class));
    }

    @Test
    @DisplayName("Test 11: Delete spending - User lookup happens after spending validation")
    void testDeleteSpending_UserLookupOrder() {
        // Given
        when(spendingRepository.findById(100L)).thenReturn(Optional.of(carChargingSpending));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        spendingService.deleteSpending(1L, 100L);

        // Then - InOrder verification
        var inOrder = inOrder(spendingRepository, userRepository);
        inOrder.verify(spendingRepository).findById(100L);
        inOrder.verify(userRepository).findById(1L);
    }
}





