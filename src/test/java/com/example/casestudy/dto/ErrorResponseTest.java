package com.example.casestudy.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ErrorResponse DTO.
 * 
 * This test class verifies that the ErrorResponse class:
 * - Works correctly with Lombok annotations
 * - Maintains proper field values
 * - Supports all constructors (no-args and all-args)
 * - Implements equals and hashCode correctly
 * - Provides proper toString representation
 */
class ErrorResponseTest {

    @Test
    void shouldCreateObjectUsingNoArgsConstructor() {
        ErrorResponse errorResponse = new ErrorResponse();
        assertNotNull(errorResponse);
    }

    @Test
    void shouldCreateObjectUsingAllArgsConstructor() {
        ErrorResponse errorResponse = new ErrorResponse(400, "Bad Request", "Invalid input");
        
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Invalid input", errorResponse.getMessage());
    }

    @Test
    void shouldSetAndGetStatus() {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(404);
        assertEquals(404, errorResponse.getStatus());
    }

    @Test
    void shouldSetAndGetError() {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError("Not Found");
        assertEquals("Not Found", errorResponse.getError());
    }

    @Test
    void shouldSetAndGetMessage() {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Resource not found");
        assertEquals("Resource not found", errorResponse.getMessage());
    }

    @Test
    void shouldBeEqualWhenAllFieldsAreSame() {
        ErrorResponse errorResponse1 = new ErrorResponse(401, "Unauthorized", "Invalid credentials");
        ErrorResponse errorResponse2 = new ErrorResponse(401, "Unauthorized", "Invalid credentials");

        assertEquals(errorResponse1, errorResponse2);
        assertEquals(errorResponse1.hashCode(), errorResponse2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenFieldsDiffer() {
        ErrorResponse errorResponse1 = new ErrorResponse(400, "Bad Request", "Invalid input");
        ErrorResponse errorResponse2 = new ErrorResponse(401, "Unauthorized", "Invalid credentials");

        assertNotEquals(errorResponse1, errorResponse2);
    }

    @Test
    void toStringShouldContainAllFields() {
        ErrorResponse errorResponse = new ErrorResponse(500, "Internal Server Error", "Something went wrong");
        String toString = errorResponse.toString();
        
        assertTrue(toString.contains("500"));
        assertTrue(toString.contains("Internal Server Error"));
        assertTrue(toString.contains("Something went wrong"));
    }

    @Test
    void shouldMaintainExactJsonStructureFields() {
        // This test verifies that the DTO maintains the exact fields required by the API contract
        ErrorResponse errorResponse = new ErrorResponse(400, "Bad Request", "Test message");
        
        // Verify all three required fields are present and accessible
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getError());
        assertNotNull(errorResponse.getMessage());
        
        // Verify field types
        assertEquals(Integer.class, Integer.valueOf(errorResponse.getStatus()).getClass());
        assertEquals(String.class, errorResponse.getError().getClass());
        assertEquals(String.class, errorResponse.getMessage().getClass());
    }
}