package com.example.casestudy.exception.common;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when rate limit is exceeded.
 * 
 * This exception is thrown when:
 * - User exceeds the allowed number of requests in a time window
 * - Rate limiter rejects the request to protect system resources
 * - Request frequency violates configured rate limit policy
 * 
 * HTTP Status: 429 TOO_MANY_REQUESTS
 * 
 * Design Notes:
 * - Extends AppException to inherit standardized exception handling
 * - Uses fixed error message to provide clear user feedback
 * - No parameterized constructor to prevent arbitrary messages
 * 
 * Rate Limiting Pattern:
 * This exception indicates that the client should reduce request frequency
 * and retry after the configured time window. Clients should implement
 * exponential backoff or respect Retry-After header if provided.
 */
public class RateLimitExceededException extends AppException {

    /**
     * Constructs a new RateLimitExceededException with a predefined message
     * and HTTP status TOO_MANY_REQUESTS (429).
     */
    public RateLimitExceededException() {
        super("Rate limit exceeded. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
    }
}
