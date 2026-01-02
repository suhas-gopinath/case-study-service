package com.example.casestudy.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void validUserRequest() {
        UserRequest request = new UserRequest("validUser_123", "Password123!");
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    @DisplayName("Should fail when username is too short")
    void usernameTooShort() {
        UserRequest request = new UserRequest("abc", "Password123!");
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username must be between 6 and 30 characters")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid space", "user!", "bad@name"})
    @DisplayName("Should fail when username contains invalid characters")
    void usernameInvalidCharacters(String invalidUsername) {
        UserRequest request = new UserRequest(invalidUsername, "Password123!");
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail when password lacks a special character")
    void passwordNoSpecialChar() {
        UserRequest request = new UserRequest("validUser", "Password123");
        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password must contain at least one uppercase letter")));
    }

    @Test
    @DisplayName("Test Lombok AllArgsConstructor and Getters")
    void testLombokMethods() {
        UserRequest request = new UserRequest("testUser", "Secret123!");
        assertEquals("testUser", request.getUsername());
        assertEquals("Secret123!", request.getPassword());
    }
}
