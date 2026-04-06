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

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    
    private final UserDatabaseService userDatabaseService;
    private final PBKDF2PasswordService passwordService;
    
    public AuthenticationServiceImpl(UserDatabaseService userDatabaseService, PBKDF2PasswordService passwordService) {
        this.userDatabaseService = userDatabaseService;
        this.passwordService = passwordService;
    }
    
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
    
    @Override
    public User authenticate(String username, String rawPassword) {
        logger.info("Authenticating user: {}", username);
        
        User user = userDatabaseService.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException());
        
        try {
            boolean isValid = passwordService.verify(rawPassword, user.getPasswordHash(), user.getSalt());
            
            if (!isValid) {
                logger.warn("Authentication failed — invalid password for user: {}", username);

                throw new InvalidCredentialsException();
            }
            
            logger.info("User authenticated successfully: {}", username);
            return user;
            
        }catch (Exception e) {
            logger.error("Error during authentication for user {}: {}", username, e.getMessage(), e);
            throw new InvalidInputException("Error verifying credentials. Please try again later.");
        }
    }
}