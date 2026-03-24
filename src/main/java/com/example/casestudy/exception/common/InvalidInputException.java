package com.example.casestudy.exception.common;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when input processing or validation fails.
 * 
 * This exception is thrown when:
 * - Password hashing fails during registration
 * - Password verification fails during authentication
 * - Input data processing encounters an error
 * - General validation or processing errors occur
 * 
 * HTTP Status: 400 BAD_REQUEST
 * 
 * Design Notes:
 * - Extends AppException to inherit standardized exception handling
 * - Accepts custom message to provide specific error details
 * - Used as a general-purpose exception for input-related errors
 * 
 * Usage:
 * Thrown by service layer when input processing fails or when
 * technical errors occur during user operations.
 */
public class InvalidInputException extends AppException {

    /**
     * Constructs a new InvalidInputException with a specific error message
     * and HTTP status BAD_REQUEST (400).
     * 
     * @param message The error message describing the input validation failure
     *                (e.g., "Error processing registration", "Error verifying credentials")
     */
    public InvalidInputException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
