package com.example.casestudy.exception.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InvalidRefreshTokenException.
 * 
 * This test class verifies:
 * - Exception message
 * - HTTP status code (401 UNAUTHORIZED)
 * - Inheritance from AppException
 */
class InvalidRefreshTokenExceptionTest {

    @Test
    @DisplayName("Should create exception with predefined message")
    void testExceptionMessage() {
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException();
        
        assertEquals("Invalid or expired refresh token", exception.getMessage());
    }

    @Test
    @DisplayName("Should have UNAUTHORIZED status")
    void testExceptionStatus() {
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException();
        
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    @DisplayName("Should have 401 status code")
    void testExceptionStatusCode() {
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException();
        
        assertEquals(401, exception.getStatus().value());
    }

    @Test
    @DisplayName("Should be instance of RuntimeException")
    void testExceptionInheritance() {
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException();
        
        assertTrue(exception instanceof RuntimeException);
    }
}
