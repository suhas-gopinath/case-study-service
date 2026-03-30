package com.example.casestudy.service.password;

import com.example.casestudy.exception.common.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * PBKDF2 implementation of the PasswordService interface.
 * 
 * This implementation uses PBKDF2WithHmacSHA256 algorithm with:
 * - 100,000 iterations (industry standard for strong security)
 * - 256-bit key length
 * - 16-byte random salt
 * - Base64 encoding for storage
 * 
 * SOLID Principles:
 * - Single Responsibility: Handles only PBKDF2 password hashing logic
 * - Open/Closed: Can be replaced with other implementations (e.g., BCrypt) without modifying clients
 * - Liskov Substitution: Fully substitutable for PasswordService interface
 * 
 * Security Notes:
 * - Uses SecureRandom.getInstanceStrong() for cryptographically secure salt generation
 * - All hashing parameters are preserved from the original implementation
 * - No behavioral changes from the original UserService implementation
 */
@Service
public class PBKDF2PasswordService implements PasswordService {
    
    private static final Logger logger = LoggerFactory.getLogger(PBKDF2PasswordService.class);
    
    // Algorithm configuration - preserved from original UserService
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    
    /**
     * Hashes a password using PBKDF2WithHmacSHA256.
     * 
     * @param password The plain text password to hash
     * @param salt The salt bytes to use for hashing
     * @return The hashed password as a Base64-encoded string
     * @throws InvalidInputException if hashing fails due to algorithm or key spec issues
     */
    @Override
    public String hash(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Error during password hashing: {}", e.getMessage(), e);
            throw new InvalidInputException("Error processing password hash");
        }
    }
    
    /**
     * Verifies a raw password against a stored hash and salt.
     * 
     * @param rawPassword The plain text password to verify
     * @param storedHash The stored password hash (Base64-encoded)
     * @param storedSalt The stored salt (Base64-encoded)
     * @return true if the password matches, false otherwise
     * @throws InvalidInputException if verification fails due to decoding or hashing issues
     */
    @Override
    public boolean verify(String rawPassword, String storedHash, String storedSalt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(storedSalt);
            String hashedAttempt = hash(rawPassword, saltBytes);
            return hashedAttempt.equals(storedHash);
        } catch (IllegalArgumentException e) {
            logger.error("Error decoding salt during password verification: {}", e.getMessage(), e);
            throw new InvalidInputException("Error verifying password");
        }
    }
    
    /**
     * Generates a cryptographically secure random salt.
     * 
     * @return A byte array containing the random salt
     * @throws InvalidInputException if salt generation fails
     */
    public byte[] generateSalt() {
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error generating salt: {}", e.getMessage(), e);
            throw new InvalidInputException("Error generating salt");
        }
    }
}