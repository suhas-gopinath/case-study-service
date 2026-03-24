package com.example.casestudy.exception.handler;


import com.example.casestudy.dto.ErrorResponse;
import com.example.casestudy.exception.auth.InvalidCredentialsException;
import com.example.casestudy.exception.auth.TokenValidationException;
import com.example.casestudy.exception.common.InvalidInputException;
import com.example.casestudy.exception.user.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle InvalidCredentialsException with UNAUTHORIZED status")
    void testHandleInvalidCredentialsException() {
        InvalidCredentialsException exception = new InvalidCredentialsException();

        ResponseEntity<Object> response = exceptionHandler.handleAppException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(401, errorResponse.getStatus());
        assertEquals("Unauthorized", errorResponse.getError());
        assertEquals("Invalid username or password", errorResponse.getMessage());
    }

    @Test
    @DisplayName("Should handle TokenValidationException with UNAUTHORIZED status")
    void testHandleTokenValidationException() {
        TokenValidationException exception = new TokenValidationException("JWT has expired");

        ResponseEntity<Object> response = exceptionHandler.handleAppException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(401, errorResponse.getStatus());
        assertEquals("Unauthorized", errorResponse.getError());
        assertEquals("JWT has expired", errorResponse.getMessage());
    }

    @Test
    @DisplayName("Should handle UserAlreadyExistsException with BAD_REQUEST status")
    void testHandleUserAlreadyExistsException() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("Username already exists: testuser");

        ResponseEntity<Object> response = exceptionHandler.handleAppException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Username already exists: testuser", errorResponse.getMessage());
    }

    @Test
    @DisplayName("Should handle InvalidInputException with BAD_REQUEST status")
    void testHandleInvalidInputException() {
        InvalidInputException exception = new InvalidInputException("Error processing registration");

        ResponseEntity<Object> response = exceptionHandler.handleAppException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Error processing registration", errorResponse.getMessage());
    }

    @Test
    @DisplayName("Should handle MissingRequestHeaderException with BAD_REQUEST status")
    void testHandleMissingRequestHeaderException() throws NoSuchMethodException {
        // Create a mock MethodParameter for the exception
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        
        MissingRequestHeaderException exception = new MissingRequestHeaderException("Authorization", parameter);

        ResponseEntity<Object> response = exceptionHandler.handleMissingHeader(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Missing required header: Authorization", errorResponse.getMessage());
    }

    @Test
    @DisplayName("Should handle generic Exception with INTERNAL_SERVER_ERROR status")
    void testHandleGenericException() {
        Exception exception = new RuntimeException("Unexpected error");

        ResponseEntity<Object> response = exceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ErrorResponse);
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
        assertEquals("Internal server error", errorResponse.getMessage());
    }

    @Test
    @DisplayName("Should maintain exact error response format")
    void testErrorResponseFormat() {
        InvalidInputException exception = new InvalidInputException("Test message");

        ResponseEntity<Object> response = exceptionHandler.handleAppException(exception);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();

        // Verify exact format: {status, error, message}
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Test message", errorResponse.getMessage());
        
        // Verify no additional fields by checking the class structure
        assertEquals(3, ErrorResponse.class.getDeclaredFields().length);
    }

    // Dummy method for MissingRequestHeaderException test
    private void dummyMethod(String header) {
        // This method is only used for creating MethodParameter in tests
    }
}
