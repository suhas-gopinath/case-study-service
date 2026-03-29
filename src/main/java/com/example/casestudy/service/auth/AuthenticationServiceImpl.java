package com.example.casestudy.service.auth;

import com.example.casestudy.dto.UserRequest;



import com.example.casestudy.exception.auth.InvalidCredentialsException;
import com.example.casestudy.exception.common.InvalidInputException;
import com.example.casestudy.exception.user.UserAlreadyExistsException;
import com.example.casestudy.model.User;
import com.example.casestudy.service.database.UserDatabaseService;
import com.example.casestudy.service.password.PBKDF2PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Implementation of AuthenticationService using password-based authentication.
 * 
 * This implementation handles:
 * - User registration with password hashing
 * - User authentication with password verification
 * - Username normalization (lowercase)
 * - Comprehensive logging for security audit
 * 
 * SOLID Principles:
 * - Single Responsibility: Handles only authentication orchestration
 * - Open/Closed: Can be extended or replaced without modifying clients
 * - Liskov Substitution: Fully substitutable for AuthenticationService interface
 * - Dependency Inversion: Depends on abstractions (PasswordService, UserDatabaseService)
 * 
 * All business logic and error handling are preserved from the original UserService.
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    
    private final UserDatabaseService userDatabaseService;
    private final PBKDF2PasswordService passwordService;
    
    public AuthenticationServiceImpl(UserDatabaseService userDatabaseService, PBKDF2PasswordService passwordService) {
        this.userDatabaseService = userDatabaseService;
        this.passwordService = passwordService;
    }
    
    /**
     * Registers a new user with hashed password.
     * 
     * Process:
     * 1. Normalize username to lowercase
     * 2. Check for existing username
     * 3. Generate salt and hash password
     * 4. Save user to database
     * 
     * @param request The user registration request
     * @return The registered User entity
     * @throws UserAlreadyExistsException if username already exists
     * @throws InvalidInputException if password hashing fails
     */
    @Override
    public User register(UserRequest request) {
        logger.info("Registering new user: {}", request.getUsername());
        String username = request.getUsername().toLowerCase();
        
        if (userDatabaseService.findByUsername(username).isPresent()) {
            logger.warn("User registration failed. username already exists: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }
        
        try {
            // Generate salt and hash password using PasswordService
            byte[] saltBytes = passwordService.generateSalt();
            String saltString = Base64.getEncoder().encodeToString(saltBytes);
            String hashedPassword = passwordService.hash(request.getPassword(), saltBytes);
            
            // Create and save new user
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setPasswordHash(hashedPassword);
            newUser.setSalt(saltString);
            
            User savedUser = userDatabaseService.save(newUser);
            logger.info("User registered successfully: {}", savedUser.getUsername());
            return savedUser;
            
        } catch (Exception e) {
            logger.error("Error during password hashing for user {}: {}", request.getUsername(), e.getMessage(), e);
            throw new InvalidInputException("Error processing registration");
        }
    }
    
    /**
     * Authenticates a user with username and password.
     * 
     * Process:
     * 1. Fetch user from database
     * 2. Verify password using PasswordService
     * 3. Return authenticated user
     * 
     * @param username The username to authenticate
     * @param rawPassword The plain text password
     * @return The authenticated User entity
     * @throws InvalidCredentialsException if username not found or password invalid
     * @throws InvalidInputException if password verification fails
     */
    @Override
    public User authenticate(String username, String rawPassword) {
        logger.info("Authenticating user: {}", username);
        
        User user = userDatabaseService.findByUsername(username)

                .orElseThrow(() -> new InvalidCredentialsException());
        
        try {
            // Verify password using PasswordService
            boolean isValid = passwordService.verify(rawPassword, user.getPasswordHash(), user.getSalt());
            
            if (!isValid) {
                logger.warn("Authentication failed — invalid password for user: {}", username);

                throw new InvalidCredentialsException();
            }
            
            logger.info("User authenticated successfully: {}", username);
            return user;
            
        } catch (InvalidCredentialsException e) {
            // Re-throw authentication failures
            throw e;
        } catch (Exception e) {
            logger.error("Error during authentication for user {}: {}", username, e.getMessage(), e);
            throw new InvalidInputException("Error verifying credentials. Please try again later.");
        }
    }
}