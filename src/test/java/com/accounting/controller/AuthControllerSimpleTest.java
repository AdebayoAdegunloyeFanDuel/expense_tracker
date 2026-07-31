package com.accounting.controller;

import com.accounting.dto.AuthResponse;
import com.accounting.dto.LoginRequest;
import com.accounting.dto.RegisterRequest;
import com.accounting.dto.UserDto;
import com.accounting.security.JwtTokenProvider;
import com.accounting.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthController
 */
@DisplayName("AuthController Tests")
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserDto userDto;
    private String testToken;

    @BeforeEach
    void setup() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        userDto = UserDto.builder()
            .id(1L)
            .email("test@example.com")
            .bonusPoints(0)
            .totalIncome(BigDecimal.ZERO)
            .totalSpending(BigDecimal.ZERO)
            .netBalance(BigDecimal.ZERO)
            .build();

        testToken = "test.jwt.token";
    }

    @Test
    @DisplayName("Test 1: Successfully register new user")
    void testRegister_Success() {
        when(userService.registerUser(any(RegisterRequest.class))).thenReturn(userDto);
        when(jwtTokenProvider.generateToken(anyLong(), anyString())).thenReturn(testToken);

        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo(testToken);
        verify(userService).registerUser(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Test 2: Register with duplicate email fails")
    void testRegister_DuplicateEmail() {
        when(userService.registerUser(any(RegisterRequest.class)))
            .thenThrow(new IllegalArgumentException("Email already registered"));

        assertThatThrownBy(() -> authController.register(registerRequest))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Test 3: Successfully login")
    void testLogin_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("1");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userService.getUserProfile(1L)).thenReturn(userDto);
        when(jwtTokenProvider.generateToken(anyLong(), anyString())).thenReturn(testToken);

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo(testToken);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Test 4: Login with wrong credentials fails")
    void testLogin_InvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authController.login(loginRequest))
            .isInstanceOf(BadCredentialsException.class);
        verify(userService, never()).getUserProfile(anyLong());
    }
}


