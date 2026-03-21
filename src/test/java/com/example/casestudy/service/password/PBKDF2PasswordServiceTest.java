package com.example.casestudy.service.password;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class PBKDF2PasswordServiceTest {

    private PBKDF2PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PBKDF2PasswordService();
    }

    @Test
    @DisplayName("Should generate salt successfully")
    void testGenerateSalt() {
        byte[] salt = passwordService.generateSalt();
        
        assertNotNull(salt);
        assertEquals(16, salt.length);
    }

    @Test
    @DisplayName("Should hash password successfully")
    void testHashPassword() {
        byte[] salt = passwordService.generateSalt();
        String password = "TestPassword123!";
        
        String hash = passwordService.hash(password, salt);
        
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    @DisplayName("Should verify correct password")
    void testVerifyCorrectPassword() {
        String password = "TestPassword123!";
        byte[] salt = passwordService.generateSalt();
        String hash = passwordService.hash(password, salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        
        boolean isValid = passwordService.verify(password, hash, saltBase64);
        
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject incorrect password")
    void testVerifyIncorrectPassword() {
        String correctPassword = "TestPassword123!";
        String wrongPassword = "WrongPassword456!";
        byte[] salt = passwordService.generateSalt();
        String hash = passwordService.hash(correctPassword, salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        
        boolean isValid = passwordService.verify(wrongPassword, hash, saltBase64);
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should produce different hashes for same password with different salts")
    void testDifferentSaltsProduceDifferentHashes() {
        String password = "TestPassword123!";
        byte[] salt1 = passwordService.generateSalt();
        byte[] salt2 = passwordService.generateSalt();
        
        String hash1 = passwordService.hash(password, salt1);
        String hash2 = passwordService.hash(password, salt2);
        
        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Should throw exception for invalid salt during verification")
    void testVerifyWithInvalidSalt() {
        String password = "TestPassword123!";
        byte[] salt = passwordService.generateSalt();
        String hash = passwordService.hash(password, salt);
        String invalidSalt = "invalid!!base64###";
        
        assertThrows(RuntimeException.class, () -> {
            passwordService.verify(password, hash, invalidSalt);
        });
    }
}