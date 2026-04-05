package com.example.casestudy.exception.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimitExceededException.
 * 
 * Test Coverage:
 * - Exception message validation
 * - HTTP status code validation
 * - Exception inheritance verification
 * - Exception instantiation
 */
class RateLimitExceededExceptionTest {

    @Test
    void testExceptionMessage() {
        // Given & When
        RateLimitExceededException exception = new RateLimitExceededException();
        
        // Then
        assertEquals("Rate limit exceeded. Please try again later.", exception.getMessage());
    }

    @Test
    void testHttpStatus() {
        // Given & When
        RateLimitExceededException exception = new RateLimitExceededException();
        
        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatus());
        assertEquals(429, exception.getStatus().value());
    }

    @Test
    void testExceptionInheritance() {
        // Given & When
        RateLimitExceededException exception = new RateLimitExceededException();
        
        // Then
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof com.example.casestudy.exception.AppException);
    }

    @Test
    void testExceptionCanBeThrown() {
        // Given & When & Then
        assertThrows(RateLimitExceededException.class, () -> {
            throw new RateLimitExceededException();
        });
    }

    @Test
    void testExceptionMessageIsNotNull() {
        // Given & When
        RateLimitExceededException exception = new RateLimitExceededException();
        
        // Then
        assertNotNull(exception.getMessage());
        assertFalse(exception.getMessage().isEmpty());
    }

    @Test
    void testHttpStatusReasonPhrase() {
        // Given & When
        RateLimitExceededException exception = new RateLimitExceededException();
        
        // Then
        assertEquals("Too Many Requests", exception.getStatus().getReasonPhrase());
    }
}
