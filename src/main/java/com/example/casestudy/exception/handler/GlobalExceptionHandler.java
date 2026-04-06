package com.example.casestudy.exception.handler;

import com.example.casestudy.dto.ErrorResponse;
import com.example.casestudy.exception.AppException;
import com.example.casestudy.exception.common.RateLimitExceededException;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Object> buildResponseEntity(String message, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            status.getReasonPhrase(),
            message
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Object> handleAppException(AppException ex) {
        HttpStatus status = ex.getStatus();
        
        // Log based on severity (client error vs server error)
        if (status.is4xxClientError()) {
            logger.warn("Client error occurred: {} - Status: {}", ex.getMessage(), status);
        } else if (status.is5xxServerError()) {
            logger.error("Server error occurred: {} - Status: {}", ex.getMessage(), status, ex);
        }
        
        return buildResponseEntity(ex.getMessage(), status);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Object> handleMissingHeader(MissingRequestHeaderException ex) {
        String message = "Missing required header: " + ex.getHeaderName();
        logger.warn("Missing request header: {}", ex.getHeaderName());
        return buildResponseEntity(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<Object> handleMissingCookie(MissingRequestCookieException ex) {
        String message = "Refresh token is missing";
        logger.warn("Missing cookie: {}", ex.getCookieName());
        return buildResponseEntity(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Object> handleRateLimitExceeded(RequestNotPermitted ex) {
        logger.warn("Rate limit exceeded for rate limiter: {}", ex.getMessage());
        RateLimitExceededException rateLimitException = new RateLimitExceededException();
        return buildResponseEntity(rateLimitException.getMessage(), rateLimitException.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return buildResponseEntity("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
