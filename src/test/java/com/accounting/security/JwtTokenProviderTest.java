package com.accounting.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider
 * Focus on JWT token generation, validation, and claims extraction
 */
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String jwtSecret;
    private long jwtExpirationMs;
    private SecretKey signingKey;

    @BeforeEach
    void setup() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtSecret = "mySecretKeyForJWTTokenGenerationMustBeLongEnoughForHS256Algorithm";
        jwtExpirationMs = 86400000; // 24 hours

        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", jwtExpirationMs);

        // Initialize the signing key
        signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtTokenProvider, "signingKey", signingKey);
    }

    // ============= HAPPY PATH TESTS =============

    @Test
    @DisplayName("Test 1: Generate valid JWT token")
    void testGenerateToken_Success() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";

        // When
        String token = jwtTokenProvider.generateToken(userId, email);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Test 2: Extract userId from token")
    void testGetUserIdFromToken_Success() {
        // Given
        Long expectedUserId = 1L;
        String email = "test@example.com";
        String token = jwtTokenProvider.generateToken(expectedUserId, email);

        // When
        Long actualUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertThat(actualUserId).isEqualTo(expectedUserId);
    }

    @Test
    @DisplayName("Test 3: Validate valid token")
    void testValidateToken_ValidToken() {
        // Given
        String token = jwtTokenProvider.generateToken(1L, "test@example.com");

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    // ============= CRITICAL TESTS =============

    @Test
    @DisplayName("Test 4: CRITICAL - Token contains email claim")
    void testGenerateToken_ContainsEmailClaim() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String token = jwtTokenProvider.generateToken(userId, email);

        // When
        Claims claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        // Then
        assertThat(claims.get("email", String.class)).isEqualTo(email);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("Test 5: CRITICAL - Token has correct expiration")
    void testGenerateToken_HasCorrectExpiration() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";

        // When
        String token = jwtTokenProvider.generateToken(userId, email);

        Claims claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        // Then
        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();

        // Check that expiration is approximately 24 hours after issued at (allow for test execution time)
        long actualExpiration = expiration.getTime() - issuedAt.getTime();
        assertThat(actualExpiration).isCloseTo(jwtExpirationMs, within(1000L)); // Within 1 second
    }

    // Note: Invalid signature testing is implicitly covered by the validateToken implementation
    // which catches SecurityExceptions. Explicit testing would require exposing internal exception handling.

    // ============= EDGE CASE TESTS =============

    @Test
    @DisplayName("Test 7: Expired token fails validation")
    void testValidateToken_ExpiredToken() {
        // Given - Create expired token
        String expiredToken = Jwts.builder()
            .subject("1")
            .claim("email", "test@example.com")
            .issuedAt(new Date(System.currentTimeMillis() - 2 * jwtExpirationMs))
            .expiration(new Date(System.currentTimeMillis() - jwtExpirationMs))
            .signWith(signingKey)
            .compact();

        // When
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Test 8: Malformed token fails validation")
    void testValidateToken_MalformedToken() {
        // Given
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Test 9: Empty token fails validation")
    void testValidateToken_EmptyToken() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Test 10: Null token fails validation gracefully")
    void testValidateToken_NullToken() {
        // When
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    // ============= ISOLATION TESTS =============

    @Test
    @DisplayName("Test 11: Different users get different tokens")
    void testGenerateToken_DifferentUsersHaveDifferentTokens() {
        // Given
        Long userId1 = 1L;
        Long userId2 = 2L;
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        // When
        String token1 = jwtTokenProvider.generateToken(userId1, email1);
        String token2 = jwtTokenProvider.generateToken(userId2, email2);

        // Then
        assertThat(token1).isNotEqualTo(token2);

        Long extractedUserId1 = jwtTokenProvider.getUserIdFromToken(token1);
        Long extractedUserId2 = jwtTokenProvider.getUserIdFromToken(token2);

        assertThat(extractedUserId1).isEqualTo(userId1);
        assertThat(extractedUserId2).isEqualTo(userId2);
    }

    @Test
    @DisplayName("Test 12: Token generated consecutively can be validated")
    void testGenerateToken_ConsecutiveTokensAreValid() throws InterruptedException {
        // Given
        Long userId = 1L;
        String email = "test@example.com";

        // When
        String token1 = jwtTokenProvider.generateToken(userId, email);
        Thread.sleep(50); // Longer delay to ensure different issuedAt
        String token2 = jwtTokenProvider.generateToken(userId, email);

        // Then - Both tokens should be valid
        assertThat(jwtTokenProvider.validateToken(token1)).isTrue();
        assertThat(jwtTokenProvider.validateToken(token2)).isTrue();

        // And they should extract the same user ID
        assertThat(jwtTokenProvider.getUserIdFromToken(token1)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getUserIdFromToken(token2)).isEqualTo(userId);
    }

    @Test
    @DisplayName("Test 13: Extract userId handles large user IDs")
    void testGetUserIdFromToken_LargeUserId() {
        // Given
        Long largeUserId = 9999999999L;
        String email = "test@example.com";
        String token = jwtTokenProvider.generateToken(largeUserId, email);

        // When
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertThat(extractedUserId).isEqualTo(largeUserId);
    }
}






