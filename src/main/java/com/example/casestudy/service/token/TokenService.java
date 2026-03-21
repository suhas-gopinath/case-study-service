package com.example.casestudy.service.token;

/**
 * Service interface for token generation and validation operations.
 * 
 * This interface provides abstraction for access token operations,
 * allowing different token implementations (JWT, OAuth, etc.) to be used
 * interchangeably without modifying dependent code.
 * 
 * SOLID Principles:
 * - Single Responsibility: Focused solely on token operations
 * - Open/Closed: Open for extension (refresh tokens, OAuth) without modification
 * - Interface Segregation: Provides only token-related methods
 * - Dependency Inversion: Clients depend on this abstraction, not concrete implementations
 * 
 * Future Extensibility:
 * - Refresh token methods can be added to this interface or a separate RefreshTokenService
 * - Multiple token types (access, refresh, API keys) can coexist
 * - No changes required to controllers when adding new token types
 */
public interface TokenService {
    
    /**
     * Generates an access token for the specified username.
     * 
     * @param username The username to encode in the token
     * @return The generated access token as a string
     */
    String generateAccessToken(String username);
    
    /**
     * Validates an access token and extracts the username.
     * 
     * @param token The access token to validate
     * @return The username extracted from the token
     * @throws com.example.casestudy.exception.TokenValidationException if token is invalid, expired, or malformed
     */
    String validateAccessToken(String token);
}