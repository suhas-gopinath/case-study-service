package com.example.casestudy.service.token;

import com.example.casestudy.exception.auth.InvalidRefreshTokenException;
import com.example.casestudy.exception.common.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis implementation of the RefreshTokenService interface.
 * 
 * This implementation uses Redis for stateful refresh token storage with the following characteristics:
 * - Tokens are opaque random UUIDs (not JWTs)
 * - Stored as key-value pairs: refresh:{token} -> username
 * - Configurable TTL (default: 7 days)
 * - Automatic expiration handled by Redis
 * 
 * SOLID Principles:
 * - Single Responsibility: Handles only refresh token operations with Redis
 * - Open/Closed: Can be extended or replaced without modifying clients
 * - Liskov Substitution: Fully substitutable for RefreshTokenService interface
 * - Dependency Inversion: Depends on StringRedisTemplate abstraction
 * 
 * Design Notes:
 * - Uses UUID for cryptographically secure random tokens
 * - Redis key prefix "refresh:" for namespace isolation
 * - TTL configured via application.properties
 * - Comprehensive logging for security audit
 * 
 * Security Considerations:
 * - Tokens are opaque and cannot be decoded
 * - Stored separately from access tokens (JWT)
 * - Automatic expiration prevents token accumulation
 * - Revocation is immediate (delete from Redis)
 */
@Service
public class RedisRefreshTokenService implements RefreshTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisRefreshTokenService.class);
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    
    private final StringRedisTemplate redisTemplate;
    private final long refreshTokenTtl;
    
    /**
     * Constructor injection for dependency inversion.
     * 
     * @param redisTemplate Spring Data Redis template for string operations
     * @param refreshTokenTtl TTL for refresh tokens in milliseconds (from application.properties)
     */
    public RedisRefreshTokenService(
            StringRedisTemplate redisTemplate,
            @Value("${refresh.token.ttl:604800000}") long refreshTokenTtl) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenTtl = refreshTokenTtl;
    }
    
    /**
     * Creates a new refresh token for the specified username.
     * 
     * Process:
     * 1. Generate a random UUID as the token
     * 2. Store in Redis with key: refresh:{token}, value: username
     * 3. Set TTL for automatic expiration
     * 
     * @param username The username to associate with the refresh token
     * @return The generated refresh token (UUID string)
     * @throws InvalidInputException if token creation fails
     */
    @Override
    public String createRefreshToken(String username) {
        logger.info("Creating refresh token for user: {}", username);
        
        // Generate cryptographically secure random token
        String token = UUID.randomUUID().toString();
        String key = REFRESH_TOKEN_PREFIX + token;
        
        try {
            // Store token with username and set TTL
            redisTemplate.opsForValue().set(key, username, refreshTokenTtl, TimeUnit.MILLISECONDS);
            logger.info("Refresh token created successfully for user: {}", username);
            return token;
        } catch (Exception e) {
            logger.error("Error creating refresh token for user {}: {}", username, e.getMessage(), e);
            throw new InvalidInputException("Error creating refresh token");
        }
    }
    
    /**
     * Validates a refresh token and returns the associated username.
     * 
     * Process:
     * 1. Check if token exists in Redis
     * 2. Retrieve associated username
     * 3. Return username if valid
     * 
     * @param token The refresh token to validate
     * @return The username associated with the token
     * @throws InvalidRefreshTokenException if token is invalid or expired
     */
    @Override
    public String validateRefreshToken(String token) {
        logger.info("Validating refresh token");
        
        String key = REFRESH_TOKEN_PREFIX + token;
        
        try {
            String username = redisTemplate.opsForValue().get(key);
            
            if (username == null) {
                logger.warn("Invalid or expired refresh token");
                throw new InvalidRefreshTokenException();
            }
            
            logger.info("Refresh token validated successfully for user: {}", username);
            return username;
        } catch (InvalidRefreshTokenException e) {
            // Re-throw validation exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error validating refresh token: {}", e.getMessage(), e);
            throw new InvalidRefreshTokenException();
        }
    }
    
    /**
     * Revokes a refresh token by removing it from Redis.
     * 
     * This method is called during logout to invalidate the refresh token.
     * If the token doesn't exist, the operation is treated as successful (idempotent).
     * 
     * @param token The refresh token to revoke
     */
    @Override
    public void revokeRefreshToken(String token) {
        logger.info("Revoking refresh token");
        
        String key = REFRESH_TOKEN_PREFIX + token;
        
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("Refresh token revoked successfully");
            } else {
                logger.info("Refresh token not found (already expired or revoked)");
            }
        } catch (Exception e) {
            logger.error("Error revoking refresh token: {}", e.getMessage(), e);
            // Don't throw exception - revocation should be idempotent
        }
    }
}