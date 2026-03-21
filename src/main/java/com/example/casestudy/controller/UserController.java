package com.example.casestudy.controller;

import com.example.casestudy.dto.MessageDto;
import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.model.User;


import com.example.casestudy.service.auth.AuthenticationService;
import com.example.casestudy.service.token.TokenService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user authentication operations.
 * 
 * SOLID Principles Applied:
 * - Single Responsibility: Handles only HTTP request/response for authentication
 * - Dependency Inversion: Depends on AuthenticationService and TokenService interfaces, not concrete implementations
 * - Open/Closed: Can work with any implementation of the service interfaces
 * 
 * Future Extensibility:
 * - Adding BCrypt: Zero changes required (swap PasswordService implementation)
 * - Adding Refresh Tokens: Zero changes required (add RefreshTokenService)
 * - Controller depends only on abstractions, making it immune to implementation changes
 */
@RestController
@RequestMapping("/users")
public class UserController {


    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final AuthenticationService authenticationService;
    private final TokenService tokenService;






    /**
     * Constructor injection for dependency inversion.
     * 
     * @param authenticationService Service for user registration and authentication
     * @param tokenService Service for token generation and validation
     */
    public UserController(AuthenticationService authenticationService, TokenService tokenService) {
        this.authenticationService = authenticationService;
        this.tokenService = tokenService;
    }

    /**
     * Registers a new user.
     * 
     * @param request The user registration request with validated username and password
     * @return ResponseEntity with success message and HTTP 201 CREATED status
     */
    @PostMapping("/register")
    public ResponseEntity<MessageDto> registerUser(@Valid @RequestBody UserRequest request) {
        logger.info("Received registration request for username: {}", request.getUsername());

        authenticationService.register(request);
        return new ResponseEntity<>(new MessageDto("User Registered Successfully"), HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and generates an access token.
     * 
     * @param request The login request with username and password
     * @return ResponseEntity with JWT access token and HTTP 200 OK status
     */
    @PostMapping("/login")
    public ResponseEntity<MessageDto> loginUser(@RequestBody UserRequest request) {
        logger.info("Received login request for username: {}", request.getUsername());


        User user = authenticationService.authenticate(request.getUsername(), request.getPassword());
        String token = tokenService.generateAccessToken(user.getUsername());
        return new ResponseEntity<>(new MessageDto(token), HttpStatus.OK);
    }

    /**
     * Verifies an access token and returns the authenticated username.
     * 
     * @param authHeader The Authorization header containing the Bearer token
     * @return ResponseEntity with verification message and HTTP 200 OK status
     */
    @GetMapping("/verify")
    public ResponseEntity<MessageDto> verify(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        String username = tokenService.validateAccessToken(token);
        return ResponseEntity.ok(new MessageDto("Successfully verified user: " + username));
    }
}
