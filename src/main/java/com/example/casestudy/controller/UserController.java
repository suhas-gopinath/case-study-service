package com.example.casestudy.controller;

import com.example.casestudy.dto.MessageDto;
import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.model.User;
import com.example.casestudy.service.token.AccessTokenService;
import com.example.casestudy.service.token.RefreshTokenService;

import com.example.casestudy.service.auth.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 days in seconds
    
    private final AuthenticationService authenticationService;
    private final AccessTokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    
    /**
     * Constructor injection for dependency inversion.
     * 
     * @param authenticationService Service for user registration and authentication
     * @param tokenService Service for token generation and validation
     * @param refreshTokenService Service for refresh token operations
     */

    public UserController(AuthenticationService authenticationService, 
                          AccessTokenService tokenService,
                          RefreshTokenService refreshTokenService) {
        this.authenticationService = authenticationService;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
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

     * Authenticates a user and generates an access token and refresh token.
     * 
     * Extended behavior:
     * - Generates JWT access token (returned in response body)
     * - Generates refresh token (stored in Redis)
     * - Sets refresh token in HTTP-only cookie
     * 
     * API Contract: UNCHANGED - still returns JWT in MessageDto
     * 
     * @param request The login request with username and password
     * @param response HTTP response for setting cookie
     * @return ResponseEntity with JWT access token and HTTP 200 OK status
     */
    @PostMapping("/login")

    public ResponseEntity<MessageDto> loginUser(@RequestBody UserRequest request, HttpServletResponse response) {
        logger.info("Received login request for username: {}", request.getUsername());


        User user = authenticationService.authenticate(request.getUsername(), request.getPassword());


        
        // Generate access token (existing behavior)
        String accessToken = tokenService.generateAccessToken(user.getUsername());
        
        // Generate refresh token and set in HTTP-only cookie (new behavior)
        String refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
        setRefreshTokenCookie(response, refreshToken);
        
        logger.info("Login successful for user: {}", user.getUsername());
        return new ResponseEntity<>(new MessageDto(accessToken), HttpStatus.OK);
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
    
    /**
     * Refreshes the access token using a valid refresh token from cookie.
     * 
     * Process:
     * 1. Read refresh token from HTTP-only cookie
     * 2. Validate refresh token via RefreshTokenService
     * 3. Generate new access token
     * 4. Return new access token in MessageDto
     * 
     * Security:
     * - Refresh token is NOT accepted in request body
     * - Refresh token is NOT exposed in response
     * - Only reads from HTTP-only cookie
     * 
     * @param refreshToken The refresh token from HTTP-only cookie
     * @return ResponseEntity with new access token and HTTP 200 OK status
     */
    @PostMapping("/refresh")
    public ResponseEntity<MessageDto> refreshToken(@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        logger.info("Received token refresh request");
        
        // Validate refresh token and get username
        String username = refreshTokenService.validateRefreshToken(refreshToken);
        
        // Generate new access token
        String newAccessToken = tokenService.generateAccessToken(username);
        
        logger.info("Token refreshed successfully for user: {}", username);
        return ResponseEntity.ok(new MessageDto(newAccessToken));
    }
    
    /**
     * Logs out the user by revoking the refresh token.
     * 
     * Process:
     * 1. Read refresh token from HTTP-only cookie
     * 2. Revoke token from Redis
     * 3. Clear the HTTP-only cookie
     * 
     * Note: Access token (JWT) cannot be revoked as it's stateless.
     * Client should discard the access token after logout.
     * 
     * @param refreshToken The refresh token from HTTP-only cookie
     * @param response HTTP response for clearing cookie
     * @return ResponseEntity with logout confirmation message
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageDto> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        logger.info("Received logout request");
        
        if (refreshToken != null) {
            // Revoke refresh token from Redis
            refreshTokenService.revokeRefreshToken(refreshToken);
        }
        
        // Clear refresh token cookie
        clearRefreshTokenCookie(response);
        
        logger.info("Logout successful");
        return ResponseEntity.ok(new MessageDto("Logged out successfully"));
    }
    
    /**
     * Returns the current authenticated user's information.
     * 
     * This endpoint validates the JWT access token and returns the username.
     * It does NOT use refresh tokens.
     * 
     * @param authHeader The Authorization header containing the Bearer token
     * @return ResponseEntity with username in MessageDto
     */
    @GetMapping("/me")
    public ResponseEntity<MessageDto> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        logger.info("Received current user request");
        
        String token = authHeader.replace("Bearer ", "");
        String username = tokenService.validateAccessToken(token);
        
        logger.info("Current user retrieved: {}", username);
        return ResponseEntity.ok(new MessageDto(username));
    }
    
    /**
     * Sets the refresh token in an HTTP-only cookie.
     * 
     * Cookie configuration:
     * - HttpOnly: true (prevents JavaScript access)
     * - Secure: false (for local development; should be true in production)
     * - SameSite: Strict (prevents CSRF attacks)
     * - Path: /users (limits cookie scope)
     * - MaxAge: 7 days
     * 
     * @param response HTTP response
     * @param refreshToken The refresh token to set
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/users");
        cookie.setMaxAge(REFRESH_TOKEN_COOKIE_MAX_AGE);
        // SameSite=Strict is set via application configuration or manually if needed
        response.addCookie(cookie);
    }
    
    /**
     * Clears the refresh token cookie.
     * 
     * Sets MaxAge to 0 to delete the cookie immediately.
     * 
     * @param response HTTP response
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/users");
        cookie.setMaxAge(0); // Delete cookie
        response.addCookie(cookie);
    }
}
