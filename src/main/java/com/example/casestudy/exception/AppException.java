package com.example.casestudy.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception class for all application-specific exceptions.
 * 
 * This abstract class provides a standardized way to handle exceptions
 * across the application by associating each exception with an HTTP status code.
 * All custom exceptions should extend this class to ensure consistent
 * exception handling and error response formatting.
 * 
 * Design Principles:
 * - Single Responsibility: Manages exception message and HTTP status
 * - Open/Closed: Open for extension through inheritance, closed for modification
 * - Liskov Substitution: All subclasses can be used interchangeably as RuntimeException
 * 
 * Usage:
 * Custom exceptions should extend this class and define their specific
 * HTTP status code in their constructor.
 */
public abstract class AppException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Constructs a new AppException with the specified message and HTTP status.
     * 
     * @param message The detailed error message
     * @param status The HTTP status code associated with this exception
     */
    protected AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Returns the HTTP status code associated with this exception.
     * 
     * @return The HTTP status code
     */
    public HttpStatus getStatus() {
        return status;
    }
}
