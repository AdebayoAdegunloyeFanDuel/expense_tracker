package com.accounting.service;

import com.accounting.dto.RegisterRequest;
import com.accounting.dto.UserDto;
import com.accounting.entity.User;
import com.accounting.repository.BonusPointsHistoryRepository;
import com.accounting.repository.IncomeRepository;
import com.accounting.repository.SpendingRepository;
import com.accounting.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 * Focus on user registration, authentication, and profile management
 */
@DisplayName("UserService Tests")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private SpendingRepository spendingRepository;

    @Mock
    private BonusPointsHistoryRepository bonusPointsHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setup() {
        testUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .password("encodedPassword")
            .bonusPoints(5)
            .createdAt(LocalDateTime.now())
            .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
    }

    // ============= HAPPY PATH TESTS =============

    @Test
    @DisplayName("Test 1: Successfully register new user")
    void testRegisterUser_Success() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(incomeRepository.getTotalIncomeByUserId(anyLong())).thenReturn(BigDecimal.ZERO);
        when(spendingRepository.getTotalSpendingByUserId(anyLong())).thenReturn(BigDecimal.ZERO);

        // When
        UserDto result = userService.registerUser(registerRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getBonusPoints()).isEqualTo(testUser.getBonusPoints());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(registerRequest.getPassword());
    }

    @Test
    @DisplayName("Test 2: Registration fails when email already exists")
    void testRegisterUser_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.registerUser(registerRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Test 3: Get user by email - Success")
    void testGetUserByEmail_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Test 4: Get user by email - Not found")
    void testGetUserByEmail_NotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserByEmail("nonexistent@example.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User not found");
    }

    // ============= CRITICAL TESTS =============

    @Test
    @DisplayName("Test 5: CRITICAL - Get user profile with financial calculations")
    void testGetUserProfile_WithFinancialData() {
        // Given
        BigDecimal totalIncome = new BigDecimal("1000.00");
        BigDecimal totalSpending = new BigDecimal("350.00");
        BigDecimal expectedNetBalance = new BigDecimal("650.00");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(incomeRepository.getTotalIncomeByUserId(1L)).thenReturn(totalIncome);
        when(spendingRepository.getTotalSpendingByUserId(1L)).thenReturn(totalSpending);

        // When
        UserDto result = userService.getUserProfile(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getTotalIncome()).isEqualByComparingTo(totalIncome);
        assertThat(result.getTotalSpending()).isEqualByComparingTo(totalSpending);
        assertThat(result.getNetBalance()).isEqualByComparingTo(expectedNetBalance);
        assertThat(result.getBonusPoints()).isEqualTo(5);
    }

    @Test
    @DisplayName("Test 6: Get user by ID - Success")
    void testGetUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Test 7: Get user by ID - Not found")
    void testGetUserById_NotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User not found");
    }

    // ============= EDGE CASE TESTS =============

    @Test
    @DisplayName("Test 8: User profile with zero income and spending")
    void testGetUserProfile_ZeroFinancials() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(incomeRepository.getTotalIncomeByUserId(1L)).thenReturn(BigDecimal.ZERO);
        when(spendingRepository.getTotalSpendingByUserId(1L)).thenReturn(BigDecimal.ZERO);

        // When
        UserDto result = userService.getUserProfile(1L);

        // Then
        assertThat(result.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalSpending()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getNetBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Test 9: User profile with negative net balance")
    void testGetUserProfile_NegativeBalance() {
        // Given
        BigDecimal totalIncome = new BigDecimal("100.00");
        BigDecimal totalSpending = new BigDecimal("200.00");
        BigDecimal expectedNetBalance = new BigDecimal("-100.00");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(incomeRepository.getTotalIncomeByUserId(1L)).thenReturn(totalIncome);
        when(spendingRepository.getTotalSpendingByUserId(1L)).thenReturn(totalSpending);

        // When
        UserDto result = userService.getUserProfile(1L);

        // Then
        assertThat(result.getNetBalance()).isEqualByComparingTo(expectedNetBalance);
    }

    // ============= ISOLATION TESTS =============

    @Test
    @DisplayName("Test 10: Password is encoded during registration")
    void testRegisterUser_PasswordEncoded() {
        // Given
        when(userRepository. existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("secureEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(incomeRepository.getTotalIncomeByUserId(anyLong())).thenReturn(BigDecimal.ZERO);
        when(spendingRepository.getTotalSpendingByUserId(anyLong())).thenReturn(BigDecimal.ZERO);

        // When
        userService.registerUser(registerRequest);

        // Then - Verify password encoder was called with the correct password
        verify(passwordEncoder).encode("password123");
        // Verify save was called
        verify(userRepository).save(any(User.class));
    }
}




