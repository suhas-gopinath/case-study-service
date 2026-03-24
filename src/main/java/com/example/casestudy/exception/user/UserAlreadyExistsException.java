package com.example.casestudy.exception.user;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to register a user with a username that already exists.
 * 
 * This exception is thrown when:
 * - A new user registration is attempted with a username that is already taken
 * - The username uniqueness constraint would be violated
 * 
 * HTTP Status: 400 BAD_REQUEST
 * 
 * Design Notes:
 * - Extends AppException to inherit standardized exception handling
 * - Accepts custom message to include the specific username that already exists
 * - Used during user registration to enforce username uniqueness
 * 
 * Usage:
 * Thrown by AuthenticationService when a duplicate username is detected
 * during the registration process.
 */
public class UserAlreadyExistsException extends AppException {

    /**
     * Constructs a new UserAlreadyExistsException with a specific error message
     * and HTTP status BAD_REQUEST (400).
     * 
     * @param message The error message indicating which username already exists
     *                (e.g., "Username already exists: john_doe")
     */
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
