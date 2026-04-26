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

@Service
public class PBKDF2PasswordService implements PasswordService {
    
    private static final Logger logger = LoggerFactory.getLogger(PBKDF2PasswordService.class);
    
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    

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
    
    @Override
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