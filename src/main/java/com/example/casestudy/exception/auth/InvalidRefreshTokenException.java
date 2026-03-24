package com.example.casestudy.exception.auth;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when refresh token validation fails.
 * 
 * This exception is thrown when:
 * - Refresh token does not exist in Redis
 * - Refresh token has expired
 * - Refresh token format is invalid
 * - Redis operation fails during validation
 * 
 * HTTP Status: 401 UNAUTHORIZED
 * 
 * Design Notes:
 * - Extends AppException to inherit standardized exception handling
 * - Uses fixed error message to avoid information leakage
 * - No parameterized constructor to prevent arbitrary messages
 * 
 * Security Consideration:
 * The error message is intentionally generic ("Invalid or expired refresh token")
 * to prevent token enumeration attacks and information disclosure.
 * 
 * Usage:
 * Thrown by RefreshTokenService implementations when refresh token validation fails,
 * ensuring consistent error responses across all refresh token operations.
 */
public class InvalidRefreshTokenException extends AppException {

    /**
     * Constructs a new InvalidRefreshTokenException with a predefined message
     * and HTTP status UNAUTHORIZED (401).
     */
    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
    }
}
