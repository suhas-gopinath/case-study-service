package com.example.casestudy.aspect;

import com.example.casestudy.annotation.IpRateLimit;
import com.example.casestudy.exception.common.RateLimitExceededException;
import com.example.casestudy.service.rateLimit.RedisIpRateLimiterService;
import com.example.casestudy.util.IpAddressUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP Aspect for IP-based rate limiting.
 * Intercepts methods annotated with @IpRateLimit.
 */
@Aspect
@Component
@Order(1) // Execute before other aspects
public class IpRateLimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(IpRateLimitAspect.class);

    private final RedisIpRateLimiterService rateLimiterService;
    private final IpAddressUtil ipAddressUtil;

    public IpRateLimitAspect(RedisIpRateLimiterService rateLimiterService,
                             IpAddressUtil ipAddressUtil) {
        this.rateLimiterService = rateLimiterService;
        this.ipAddressUtil = ipAddressUtil;
    }

    @Around("@annotation(ipRateLimit)")
    public Object applyIpRateLimit(ProceedingJoinPoint joinPoint, IpRateLimit ipRateLimit) throws Throwable {
        String ipAddress = extractIpAddress();

        int limitForPeriod = ipRateLimit.limitForPeriod();
        long limitRefreshPeriodSeconds = ipRateLimit.limitRefreshPeriodSeconds();

        if (!rateLimiterService.isRequestPermitted(ipAddress, limitForPeriod, limitRefreshPeriodSeconds)) {
            long timeUntilReset = rateLimiterService.getTimeUntilReset(ipAddress);

            logger.warn("Rate limit exceeded for IP {} (reset in {}s)", ipAddress, timeUntilReset);
            throw new RateLimitExceededException("Rate limit exceeded for IP: " + ipAddress);
        }

        return joinPoint.proceed();
    }

    private String extractIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new IllegalStateException("No request context available");
        }

        HttpServletRequest request = attributes.getRequest();
        return ipAddressUtil.getClientIpAddress(request);
    }
}