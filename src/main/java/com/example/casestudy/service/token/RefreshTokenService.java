package com.example.casestudy.service.token;

/**
 * Service interface for refresh token operations.
 * 
 * This interface provides abstraction for refresh token management,
 * allowing different implementations (Redis, Database, etc.) to be used
 * interchangeably without modifying dependent code.
 * 
 * SOLID Principles:
 * - Single Responsibility: Focused solely on refresh token operations
 * - Open/Closed: Open for extension (new storage mechanisms) without modification
 * - Interface Segregation: Provides only refresh token-related methods
 * - Dependency Inversion: Clients depend on this abstraction, not concrete implementations
 * 
 * Design Notes:
 * - Refresh tokens are opaque random strings (not JWTs)
 * - Tokens are stateful and stored in external storage (Redis)
 * - Separate from AccessTokenService to maintain single responsibility
 * 
 * Future Extensibility:
 * - Can add methods for token rotation, family tracking, or device management
 * - Multiple storage implementations can coexist
 * - No changes required to controllers when switching implementations
 */
public interface RefreshTokenService {
    
    /**
     * Creates a new refresh token for the specified username.
     * 
     * The implementation should:
     * - Generate a cryptographically secure random token (UUID or secure random)
     * - Store the token with username mapping in external storage
     * - Set appropriate TTL (e.g., 7 days)
     * 
     * @param username The username to associate with the refresh token
     * @return The generated refresh token as an opaque string
     */
    String createRefreshToken(String username);
    
    /**
     * Validates a refresh token and returns the associated username.
     * 
     * The implementation should:
     * - Check if the token exists in storage
     * - Verify the token has not expired
     * - Return the associated username
     * 
     * @param token The refresh token to validate
     * @return The username associated with the token
     * @throws com.example.casestudy.exception.auth.InvalidRefreshTokenException if token is invalid or expired
     */
    String validateRefreshToken(String token);
    
    /**
     * Revokes a refresh token by removing it from storage.
     * 
     * This method is used during logout to invalidate the refresh token.
     * The implementation should:
     * - Remove the token from storage
     * - Handle cases where token doesn't exist gracefully
     * 
     * @param token The refresh token to revoke
     */
    void revokeRefreshToken(String token);
}
