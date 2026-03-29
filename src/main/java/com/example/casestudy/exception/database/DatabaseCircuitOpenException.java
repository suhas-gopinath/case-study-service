package com.example.casestudy.exception.database;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the database circuit breaker is in OPEN state.
 * 
 * This exception is thrown when:
 * - The circuit breaker has detected too many failures
 * - The failure rate threshold has been exceeded
 * - The circuit is preventing calls to protect the database
 * 
 * HTTP Status: 503 SERVICE_UNAVAILABLE
 * 
 * Design Notes:
 * - Extends AppException to inherit standardized exception handling
 * - Uses fixed error message to provide clear user feedback
 * - No parameterized constructor to prevent arbitrary messages
 * 
 * Circuit Breaker Pattern:
 * This exception indicates that the system is temporarily rejecting requests
 * to allow the database to recover from failures. Clients should retry after
 * the wait duration specified in circuit breaker configuration.
 */
public class DatabaseCircuitOpenException extends AppException {

    /**
     * Constructs a new DatabaseCircuitOpenException with a predefined message
     * and HTTP status SERVICE_UNAVAILABLE (503).
     */
    public DatabaseCircuitOpenException() {
        super("Database service is temporarily unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
