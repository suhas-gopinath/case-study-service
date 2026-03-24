package com.example.casestudy.exception.auth;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when JWT token validation fails.
 * 
 * This exception is thrown when:
 * - JWT token has expired
 * - JWT token signature is invalid
 * - JWT token is malformed or missing
 * - JWT token is unsupported
 * 
 * HTTP Status: 401 UNAUTHORIZED
 * 
 * Design Notes:
 * - Extends AppException to inherit standardized exception handling
 * - Accepts custom message to provide specific token validation error details

 * - Used by JwtAccessTokenService for all token validation failures
 * 
 * Usage:
 * Thrown by TokenService implementations when token validation fails,
 * providing specific error messages for different validation failure scenarios.
 */
public class TokenValidationException extends AppException {

    /**
     * Constructs a new TokenValidationException with a specific error message
     * and HTTP status UNAUTHORIZED (401).
     * 
     * @param message The specific token validation error message
     *                (e.g., "JWT has expired", "Invalid JWT signature")
     */
    public TokenValidationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
