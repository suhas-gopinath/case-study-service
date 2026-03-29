package com.example.casestudy.exception.database;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a database operation times out.
 * 
 * This exception is thrown when:
 * - Database query execution exceeds timeout threshold
 * - Connection to database cannot be established in time
 * - DataAccessException indicates timeout-related issues
 * 
 * HTTP Status: 504 GATEWAY_TIMEOUT
 * 
 * Design Notes:
 * - Extends AppException to inherit standardized exception handling
 * - Uses fixed error message to avoid information leakage
 * - No parameterized constructor to prevent arbitrary messages
 * 
 * Performance Consideration:
 * This exception helps identify slow database operations that may indicate
 * performance issues, connection pool exhaustion, or database overload.
 */
public class DatabaseTimeoutException extends AppException {

    /**
     * Constructs a new DatabaseTimeoutException with a predefined message
     * and HTTP status GATEWAY_TIMEOUT (504).
     */
    public DatabaseTimeoutException() {
        super("Database operation timed out", HttpStatus.GATEWAY_TIMEOUT);
    }
}
