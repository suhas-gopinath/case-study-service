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

@Service
public class RedisRefreshTokenService implements RefreshTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisRefreshTokenService.class);
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    
    private final StringRedisTemplate redisTemplate;
    private final long refreshTokenTtl;
    
    public RedisRefreshTokenService(
            StringRedisTemplate redisTemplate,
            @Value("${refresh.token.ttl:604800000}") long refreshTokenTtl) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenTtl = refreshTokenTtl;
    }
    
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