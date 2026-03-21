package com.example.casestudy.service.auth;

import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.model.User;

/**
 * Service interface for user authentication operations.
 * 
 * This interface provides abstraction for user registration and authentication,
 * allowing different authentication strategies to be implemented without
 * modifying dependent code.
 * 
 * SOLID Principles:
 * - Single Responsibility: Focused solely on authentication operations
 * - Open/Closed: Open for extension (new auth methods) without modification
 * - Interface Segregation: Provides only authentication-related methods
 * - Dependency Inversion: Clients depend on this abstraction, not concrete implementations
 * 
 * Future Extensibility:
 * - Refresh token authentication can be added without modifying this interface
 * - Multi-factor authentication can be integrated through new methods
 * - No changes required to controllers when adding new authentication mechanisms
 */
public interface AuthenticationService {
    
    /**
     * Registers a new user with the provided credentials.
     * 
     * @param request The user registration request containing username and password
     * @return The registered User entity with generated ID
     * @throws com.example.casestudy.exception.UserAlreadyExistsException if username already exists
     * @throws com.example.casestudy.exception.InvalidInputException if registration processing fails
     */
    User register(UserRequest request);
    
    /**
     * Authenticates a user with username and password.
     * 
     * @param username The username to authenticate
     * @param password The plain text password to verify
     * @return The authenticated User entity
     * @throws com.example.casestudy.exception.InvalidCredentialsException if credentials are invalid
     * @throws com.example.casestudy.exception.InvalidInputException if authentication processing fails
     */
    User authenticate(String username, String password);
}