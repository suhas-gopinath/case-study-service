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

/**
 * Global exception handler for the application.
 * 
 * This class provides centralized exception handling across all @RequestMapping methods
 * through @ExceptionHandler methods. It ensures consistent error response formatting
 * and maintains the exact API contract for error responses.
 * 
 * Error Response Format (MUST remain exactly as specified):
 * {
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Some message"
 * }
 * 
 * Design Principles:
 * - Single Responsibility: Handles all exception-to-HTTP-response conversions
 * - Open/Closed: Can handle new exceptions by extending AppException
 * - DRY: Single handler method for all AppException subclasses
 * 
 * Security Considerations:
 * - Generic exception messages prevent information leakage
 * - Stack traces are never exposed to clients
 * - Detailed error information is logged server-side only
 * 
 * Improvements over previous implementation:
 * - Single handler for all AppException subclasses (DRY principle)
 * - Comprehensive logging for debugging and monitoring
 * - Improved missing header handling with specific header name
 * - Secure generic exception handling without exposing internal details
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Builds a standardized error response entity.
     * 
     * This method creates an ErrorResponse DTO and wraps it in a ResponseEntity
     * with the appropriate HTTP status code. The response format is maintained
     * exactly as required by the API contract.
     * 
     * @param message The error message to include in the response
     * @param status The HTTP status code for the response
     * @return ResponseEntity containing the error response
     */
    private ResponseEntity<Object> buildResponseEntity(String message, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            status.getReasonPhrase(),
            message
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handles all application-specific exceptions that extend AppException.
     * 
     * This single handler method replaces individual handlers for each exception type,
     * following the DRY (Don't Repeat Yourself) principle. It extracts the HTTP status
     * and message from the exception and builds a standardized response.
     * 
     * Logging:
     * - WARN level for client errors (4xx status codes)
     * - ERROR level for server errors (5xx status codes)
     * 
     * @param ex The application exception
     * @return ResponseEntity with error details and appropriate HTTP status
     */
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

    /**
     * Handles missing request header exceptions.
     * 
     * This handler provides specific error messages indicating which required header
     * is missing, improving API usability and debugging.
     * 
     * @param ex The missing request header exception
     * @return ResponseEntity with error details and HTTP 400 BAD_REQUEST status
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Object> handleMissingHeader(MissingRequestHeaderException ex) {
        String message = "Missing required header: " + ex.getHeaderName();
        logger.warn("Missing request header: {}", ex.getHeaderName());
        return buildResponseEntity(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles missing request cookie exceptions.
     * 
     * This handler provides specific error messages for missing cookies,
     * particularly for refresh token authentication.
     * 
     * Security:
     * - Generic error message prevents information leakage
     * - Returns 401 UNAUTHORIZED for authentication-related cookies
     * 
     * @param ex The missing request cookie exception
     * @return ResponseEntity with error details and HTTP 401 UNAUTHORIZED status
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<Object> handleMissingCookie(MissingRequestCookieException ex) {
        String message = "Refresh token is missing";
        logger.warn("Missing cookie: {}", ex.getCookieName());
        return buildResponseEntity(message, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles rate limit exceeded exceptions from Resilience4j.
     * 
     * This handler converts Resilience4j's RequestNotPermitted exception
     * into our custom RateLimitExceededException, ensuring consistent
     * error response formatting across the application.
     * 
     * Security:
     * - Generic error message prevents information leakage
     * - Returns 429 TOO_MANY_REQUESTS to indicate rate limiting
     * 
     * @param ex The RequestNotPermitted exception from Resilience4j
     * @return ResponseEntity with error details and HTTP 429 TOO_MANY_REQUESTS status
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Object> handleRateLimitExceeded(RequestNotPermitted ex) {
        logger.warn("Rate limit exceeded for rate limiter: {}", ex.getMessage());
        RateLimitExceededException rateLimitException = new RateLimitExceededException();
        return buildResponseEntity(rateLimitException.getMessage(), rateLimitException.getStatus());
    }

    /**
     * Handles all unhandled exceptions.
     * 
     * This is a catch-all handler for any exceptions not specifically handled by other
     * @ExceptionHandler methods. It ensures that no exception details are leaked to clients
     * while logging the full exception server-side for debugging.
     * 
     * Security:
     * - Generic error message prevents information leakage
     * - No stack traces exposed to clients
     * - Full exception details logged server-side
     * 
     * @param ex The unhandled exception
     * @return ResponseEntity with generic error message and HTTP 500 INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return buildResponseEntity("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
