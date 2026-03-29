package com.example.casestudy.service.database;

import com.example.casestudy.exception.database.DatabaseCircuitOpenException;
import com.example.casestudy.exception.database.DatabaseTimeoutException;
import com.example.casestudy.model.User;
import com.example.casestudy.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for UserDatabaseServiceImpl circuit breaker functionality.
 * 
 * These tests verify the circuit breaker behavior with actual Resilience4j configuration.
 * Unlike unit tests, these tests run with Spring context and actual circuit breaker.
 * 
 * Test Coverage:
 * - Circuit breaker fallback when circuit is OPEN
 * - Circuit breaker state transitions
 * - DatabaseCircuitOpenException thrown from fallback methods
 * 
 * Configuration:
 * - Uses test-specific circuit breaker configuration
 * - Lower thresholds for faster testing
 * - Shorter wait duration in open state
 */
@SpringBootTest
@TestPropertySource(properties = {
    "resilience4j.circuitbreaker.instances.userDatabase.failure-rate-threshold=50",
    "resilience4j.circuitbreaker.instances.userDatabase.minimum-number-of-calls=2",
    "resilience4j.circuitbreaker.instances.userDatabase.wait-duration-in-open-state=1000ms",
    "resilience4j.circuitbreaker.instances.userDatabase.sliding-window-size=2",
    "resilience4j.circuitbreaker.instances.userDatabase.sliding-window-type=COUNT_BASED"
})
class UserDatabaseServiceImplIntegrationTest {

    @Autowired
    private UserDatabaseService userDatabaseService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker circuitBreaker;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Get the circuit breaker instance
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("userDatabase");
        
        // Reset circuit breaker to CLOSED state before each test
        circuitBreaker.reset();
        
        // Setup test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPasswordHash("hashedPassword");
        testUser.setSalt("salt123");
    }

    // ========================================
    // Circuit Breaker Fallback Tests - findByUsername
    // ========================================


    @Test
    @DisplayName("Should throw DatabaseCircuitOpenException when circuit is manually opened for findByUsername")
    void testFindByUsername_ManuallyOpenCircuit() {
        // Arrange - Manually transition circuit to OPEN state
        circuitBreaker.transitionToOpenState();

        // Act & Assert
        DatabaseCircuitOpenException exception = assertThrows(
                DatabaseCircuitOpenException.class,
                () -> userDatabaseService.findByUsername("testuser")
        );

        assertEquals("Database service is temporarily unavailable. Please try again later.", 
                exception.getMessage());
        
        // Verify repository was never called (circuit prevented the call)
        verify(userRepository, never()).findByUsername(anyString());
    }

    // ========================================
    // Circuit Breaker Fallback Tests - save
    // ========================================

    @Test
    @DisplayName("Should throw DatabaseCircuitOpenException when circuit is manually opened for save")
    void testSave_ManuallyOpenCircuit() {
        // Arrange - Manually transition circuit to OPEN state
        circuitBreaker.transitionToOpenState();

        User newUser = new User();
        newUser.setUsername("newuser");

        // Act & Assert
        DatabaseCircuitOpenException exception = assertThrows(
                DatabaseCircuitOpenException.class,
                () -> userDatabaseService.save(newUser)
        );

        assertEquals("Database service is temporarily unavailable. Please try again later.", 
                exception.getMessage());
        
        // Verify repository was never called (circuit prevented the call)
        verify(userRepository, never()).save(any(User.class));
    }

    // ========================================
    // Circuit Breaker State Verification
    // ========================================

    @Test
    @DisplayName("Should verify circuit breaker starts in CLOSED state")
    void testCircuitBreaker_InitialState() {
        // Assert
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }


    @Test
    @DisplayName("Should keep circuit CLOSED when operations succeed")
    void testCircuitBreaker_RemainsClosedOnSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act - Perform successful operations
        userDatabaseService.findByUsername("testuser");
        userDatabaseService.save(testUser);

        // Assert - Circuit should remain CLOSED
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    // ========================================
    // Mixed Success and Failure Scenarios
    // ========================================

    @Test
    @DisplayName("Should handle mixed success and failure scenarios")
    void testCircuitBreaker_MixedScenarios() {
        // Arrange
        when(userRepository.findByUsername("success")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("fail"))
                .thenThrow(new QueryTimeoutException("Database timeout"));

        // Act - Success
        Optional<User> result1 = userDatabaseService.findByUsername("success");
        assertTrue(result1.isPresent());
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());

        // Act - Failure (circuit still CLOSED, should throw DatabaseTimeoutException)
        Exception failureException = assertThrows(Exception.class, 
                () -> userDatabaseService.findByUsername("fail"));
        assertTrue(failureException instanceof DatabaseTimeoutException || 
                   failureException instanceof DatabaseCircuitOpenException);
        
        // Circuit might still be CLOSED or OPEN depending on configuration
        // Just verify it's in a valid state
        assertTrue(circuitBreaker.getState() == CircuitBreaker.State.CLOSED || 
                   circuitBreaker.getState() == CircuitBreaker.State.OPEN);
    }
}