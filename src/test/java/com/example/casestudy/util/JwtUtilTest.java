package com.example.casestudy.util;
import com.example.casestudy.config.JwtConfig;
import com.example.casestudy.exception.TokenValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {
        jwtConfig = Mockito.mock(JwtConfig.class);
        String SECRET = "secretKeyForTestingThatIsLongEnoughForHS256";
        when(jwtConfig.getSecretKey()).thenReturn(SECRET);
        when(jwtConfig.getExpirationTime()).thenReturn(3600000L);

        jwtUtil = new JwtUtil(jwtConfig);
    }

    @Test
    @DisplayName("Should generate a valid token and extract the correct username")
    void generateAndValidateToken() {
        String username = "testUser";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        String validatedUser = jwtUtil.validateToken(token);
        assertEquals(username, validatedUser);
    }

    @Test
    @DisplayName("Should throw TokenValidationException when token is malformed")
    void validateMalformedToken() {
        String malformedToken = "not.a.real.token";

        TokenValidationException exception = assertThrows(TokenValidationException.class, () -> {
            jwtUtil.validateToken(malformedToken);
        });

        assertEquals("Malformed JWT token", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw TokenValidationException when token is expired")
    void validateExpiredToken() {
        //negative value for immediate expiration
        when(jwtConfig.getExpirationTime()).thenReturn(-1000L);

        String token = jwtUtil.generateToken("expiredUser");

        TokenValidationException exception = assertThrows(TokenValidationException.class, () -> {
            jwtUtil.validateToken(token);
        });

        assertEquals("JWT has expired", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when signed with a different key")
    void validateInvalidSignature() {
        String token = jwtUtil.generateToken("user");

        JwtConfig wrongKeyConfig = Mockito.mock(JwtConfig.class);
        when(wrongKeyConfig.getSecretKey()).thenReturn("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI");
        JwtUtil secondUtil = new JwtUtil(wrongKeyConfig);

        assertThrows(TokenValidationException.class, () -> {
            secondUtil.validateToken(token);
        });
    }
}