package com.example.casestudy.exception.auth;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user authentication fails due to invalid credentials.
 * 
 * This exception is thrown when:
 * - Username does not exist in the system
 * - Password does not match the stored hash
 * - Authentication verification fails
 * 
 * HTTP Status: 401 UNAUTHORIZED
 * 
 * Design Notes:
 * - Extends AppException to inherit standardized exception handling
 * - Uses fixed error message to avoid information leakage
 * - No parameterized constructor to prevent arbitrary messages
 * 
 * Security Consideration:
 * The error message is intentionally generic ("Invalid username or password")
 * to prevent username enumeration attacks.
 */
public class InvalidCredentialsException extends AppException {

    /**
     * Constructs a new InvalidCredentialsException with a predefined message
     * and HTTP status UNAUTHORIZED (401).
     */
    public InvalidCredentialsException() {
        super("Invalid username or password", HttpStatus.UNAUTHORIZED);
    }
}
