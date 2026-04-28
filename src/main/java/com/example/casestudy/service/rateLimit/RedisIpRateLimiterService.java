package com.example.casestudy.service.rateLimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisIpRateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(RedisIpRateLimiterService.class);
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    private final StringRedisTemplate redisTemplate;

    public RedisIpRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isRequestPermitted(String rateLimitKey, int limitForPeriod, long limitRefreshPeriodSeconds) {
        String key = RATE_LIMIT_KEY_PREFIX + rateLimitKey;
        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);
            if (currentCount == null) {
                logger.error("Redis increment returned null for key: {}", rateLimitKey);
                return true;
            }
            if (currentCount == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(limitRefreshPeriodSeconds));
            }
            boolean permitted = currentCount <= limitForPeriod;
            if (!permitted) {
                logger.warn("Rate limit exceeded for key: {} (count: {}/{})", rateLimitKey, currentCount, limitForPeriod);
            }
            return permitted;
        } catch (Exception e) {
            logger.error("Error checking rate limit for key {}: {}", rateLimitKey, e.getMessage(), e);
            return true;
        }
    }

    public long getTimeUntilReset(String rateLimitKey) {
        String key = RATE_LIMIT_KEY_PREFIX + rateLimitKey;
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            logger.error("Error getting TTL for key {}: {}", rateLimitKey, e.getMessage(), e);
            return 0;
        }
    }
}