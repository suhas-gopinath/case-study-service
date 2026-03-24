package com.example.casestudy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis configuration for refresh token storage.
 * 
 * This configuration class sets up Spring Data Redis for storing refresh tokens.
 * It uses StringRedisTemplate for simple key-value operations with string data.
 * 
 * Configuration Details:
 * - Uses Spring Boot auto-configuration for RedisConnectionFactory
 * - Default connection: localhost:6379 (configured in application.properties)
 * - No authentication required (development setup)
 * - StringRedisTemplate for storing token-to-username mappings
 * 
 * SOLID Principles:
 * - Single Responsibility: Configures only Redis-related beans
 * - Dependency Inversion: Uses Spring's RedisConnectionFactory abstraction
 * 
 * Design Notes:
 * - Redis connection properties are externalized to application.properties
 * - StringRedisTemplate is preferred for refresh token storage (simple string operations)
 * - TTL is managed at the service layer, not in configuration
 */
@Configuration
public class RedisConfig {

    /**
     * Creates a StringRedisTemplate bean for refresh token operations.
     * 
     * StringRedisTemplate is a specialized RedisTemplate that works with String keys and values.
     * It's ideal for storing refresh tokens as key-value pairs:
     * - Key: refresh:{token}
     * - Value: username
     * 
     * @param connectionFactory The Redis connection factory (auto-configured by Spring Boot)
     * @return StringRedisTemplate instance for refresh token operations
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
