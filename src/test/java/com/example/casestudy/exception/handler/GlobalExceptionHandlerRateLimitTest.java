package com.example.casestudy.exception.handler;

import com.example.casestudy.dto.ErrorResponse;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler's rate limit handling.
 * 
 * Test Coverage:
 * - RequestNotPermitted exception handling
 * - HTTP status code validation
 * - Error response structure validation
 * - Exception message mapping
 */
class GlobalExceptionHandlerRateLimitTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleRateLimitExceeded_ReturnsCorrectStatus() {
        // Given
        RequestNotPermitted exception = mock(RequestNotPermitted.class);
        when(exception.getMessage()).thenReturn("globalRateLimiter");
        
        // When
        ResponseEntity<Object> response = exceptionHandler.handleRateLimitExceeded(exception);
        
        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(429, response.getStatusCode().value());
    }

    @Test
    void testHandleRateLimitExceeded_ReturnsCorrectMessage() {
        // Given
        RequestNotPermitted exception = mock(RequestNotPermitted.class);
        when(exception.getMessage()).thenReturn("globalRateLimiter");
        
        // When
        ResponseEntity<Object> response = exceptionHandler.handleRateLimitExceeded(exception);
        
        // Then
        assertNotNull(response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Rate limit exceeded. Please try again later.", errorResponse.getMessage());
    }

    @Test
    void testHandleRateLimitExceeded_ReturnsCorrectErrorResponse() {
        // Given
        RequestNotPermitted exception = mock(RequestNotPermitted.class);
        when(exception.getMessage()).thenReturn("globalRateLimiter");
        
        // When
        ResponseEntity<Object> response = exceptionHandler.handleRateLimitExceeded(exception);
        
        // Then
        assertNotNull(response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(429, errorResponse.getStatus());
        assertEquals("Too Many Requests", errorResponse.getError());
        assertEquals("Rate limit exceeded. Please try again later.", errorResponse.getMessage());
    }

    @Test
    void testHandleRateLimitExceeded_WithDifferentRateLimiterNames() {
        // Given
        RequestNotPermitted exception1 = mock(RequestNotPermitted.class);
        when(exception1.getMessage()).thenReturn("loginRateLimiter");
        
        RequestNotPermitted exception2 = mock(RequestNotPermitted.class);
        when(exception2.getMessage()).thenReturn("registerRateLimiter");
        
        // When
        ResponseEntity<Object> response1 = exceptionHandler.handleRateLimitExceeded(exception1);
        ResponseEntity<Object> response2 = exceptionHandler.handleRateLimitExceeded(exception2);
        
        // Then
        ErrorResponse errorResponse1 = (ErrorResponse) response1.getBody();
        ErrorResponse errorResponse2 = (ErrorResponse) response2.getBody();
        
        assertEquals(errorResponse1.getMessage(), errorResponse2.getMessage());
        assertEquals(errorResponse1.getStatus(), errorResponse2.getStatus());
    }

    @Test
    void testHandleRateLimitExceeded_ResponseBodyIsNotNull() {
        // Given
        RequestNotPermitted exception = mock(RequestNotPermitted.class);
        when(exception.getMessage()).thenReturn("globalRateLimiter");
        
        // When
        ResponseEntity<Object> response = exceptionHandler.handleRateLimitExceeded(exception);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ErrorResponse);
    }
}
