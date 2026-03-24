package com.example.casestudy.service.token;

import com.example.casestudy.exception.auth.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RedisRefreshTokenService.
 * 
 * This test class verifies:
 * - Refresh token creation and storage in Redis
 * - Token validation and username retrieval
 * - Token revocation
 * - TTL configuration
 * - Error handling for invalid tokens
 */
class RedisRefreshTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisRefreshTokenService refreshTokenService;

    private static final long REFRESH_TOKEN_TTL = 604800000L; // 7 days in milliseconds

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        refreshTokenService = new RedisRefreshTokenService(redisTemplate, REFRESH_TOKEN_TTL);
    }

    @Test
    @DisplayName("Should create refresh token and store in Redis with TTL")
    void testCreateRefreshToken_Success() {
        String username = "testUser";

        String token = refreshTokenService.createRefreshToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify Redis storage with correct key prefix and TTL
        verify(valueOperations, times(1)).set(
            eq("refresh:" + token),
            eq(username),
            eq(REFRESH_TOKEN_TTL),
            eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("Should generate unique tokens for same user")
    void testCreateRefreshToken_UniqueTokens() {
        String username = "testUser";

        String token1 = refreshTokenService.createRefreshToken(username);
        String token2 = refreshTokenService.createRefreshToken(username);

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should validate refresh token and return username")
    void testValidateRefreshToken_Success() {
        String token = "valid-token-uuid";
        String username = "testUser";
        
        when(valueOperations.get("refresh:" + token)).thenReturn(username);

        String result = refreshTokenService.validateRefreshToken(token);

        assertEquals(username, result);
        verify(valueOperations, times(1)).get("refresh:" + token);
    }

    @Test
    @DisplayName("Should throw InvalidRefreshTokenException for non-existent token")
    void testValidateRefreshToken_TokenNotFound() {
        String token = "non-existent-token";
        
        when(valueOperations.get("refresh:" + token)).thenReturn(null);

        assertThrows(InvalidRefreshTokenException.class, () -> {
            refreshTokenService.validateRefreshToken(token);
        });
    }

    @Test
    @DisplayName("Should throw InvalidRefreshTokenException for expired token")
    void testValidateRefreshToken_ExpiredToken() {
        String token = "expired-token";
        
        // Redis returns null for expired keys
        when(valueOperations.get("refresh:" + token)).thenReturn(null);

        assertThrows(InvalidRefreshTokenException.class, () -> {
            refreshTokenService.validateRefreshToken(token);
        });
    }

    @Test
    @DisplayName("Should handle Redis exception during validation")
    void testValidateRefreshToken_RedisException() {
        String token = "test-token";
        
        when(valueOperations.get("refresh:" + token)).thenThrow(new RuntimeException("Redis connection error"));

        assertThrows(InvalidRefreshTokenException.class, () -> {
            refreshTokenService.validateRefreshToken(token);
        });
    }

    @Test
    @DisplayName("Should revoke refresh token successfully")
    void testRevokeRefreshToken_Success() {
        String token = "token-to-revoke";
        
        when(redisTemplate.delete("refresh:" + token)).thenReturn(true);

        assertDoesNotThrow(() -> {
            refreshTokenService.revokeRefreshToken(token);
        });

        verify(redisTemplate, times(1)).delete("refresh:" + token);
    }

    @Test
    @DisplayName("Should handle revocation of non-existent token gracefully")
    void testRevokeRefreshToken_TokenNotFound() {
        String token = "non-existent-token";
        
        when(redisTemplate.delete("refresh:" + token)).thenReturn(false);

        // Should not throw exception - revocation is idempotent
        assertDoesNotThrow(() -> {
            refreshTokenService.revokeRefreshToken(token);
        });

        verify(redisTemplate, times(1)).delete("refresh:" + token);
    }

    @Test
    @DisplayName("Should handle Redis exception during revocation gracefully")
    void testRevokeRefreshToken_RedisException() {
        String token = "test-token";
        
        when(redisTemplate.delete("refresh:" + token)).thenThrow(new RuntimeException("Redis connection error"));

        // Should not throw exception - revocation should be resilient
        assertDoesNotThrow(() -> {
            refreshTokenService.revokeRefreshToken(token);
        });
    }

    @Test
    @DisplayName("Should use correct Redis key prefix")
    void testRedisKeyPrefix() {
        String username = "testUser";
        String token = refreshTokenService.createRefreshToken(username);

        // Verify the key starts with "refresh:" prefix
        verify(valueOperations).set(
            argThat(key -> key.startsWith("refresh:")),
            eq(username),
            anyLong(),
            any(TimeUnit.class)
        );
    }
}
