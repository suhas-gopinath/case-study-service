package com.example.CaseStudy.service;

import com.example.CaseStudy.dto.UserRequest;
import com.example.CaseStudy.exception.InvalidCredentialsException;
import com.example.CaseStudy.exception.InvalidInputException;
import com.example.CaseStudy.exception.UserAlreadyExistsException;
import com.example.CaseStudy.model.User;
import com.example.CaseStudy.repository.UserRepository;
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
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user.
     * Throws UserAlreadyExistsException if username exists.
     * Throws InvalidInputException for hashing errors.
     */
    public User registerUser(UserRequest request) {
        logger.info("Registering new user: {}", request.getUsername());
        String username = request.getUsername().toLowerCase();
        if (userRepository.findByUsername(username).isPresent()) {
            logger.warn("User registration failed. username already exists: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        try {
            byte[] saltBytes = generateSalt();
            String saltString = Base64.getEncoder().encodeToString(saltBytes);
            String hashedPassword = hashPassword(request.getPassword(), saltBytes);

            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setPasswordHash(hashedPassword);
            newUser.setSalt(saltString);

            User savedUser = userRepository.save(newUser);
            logger.info("User registered successfully: {}", savedUser.getUsername());
            return savedUser;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Error during password hashing for user {}: {}", request.getUsername(), e.getMessage(), e);
            throw new InvalidInputException("Error processing registration. Please try again later.");
        }
    }

    /**
     * Authenticates a user.
     * Throws InvalidCredentialsException if username or password is incorrect.
     */
    public User authenticateUser(String username, String rawPassword) {
        logger.info("Authenticating user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password."));

        try {
            byte[] storedSaltBytes = Base64.getDecoder().decode(user.getSalt());
            String hashedAttemptedPassword = hashPassword(rawPassword, storedSaltBytes);

            if (!hashedAttemptedPassword.equals(user.getPasswordHash())) {
                logger.warn("Authentication failed — invalid password for user: {}", username);
                throw new InvalidCredentialsException("Invalid username or password.");
            }

            logger.info("User authenticated successfully: {}", username);
            return user;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            logger.error("Error during authentication for user {}: {}", username, e.getMessage(), e);
            throw new InvalidInputException("Error verifying credentials. Please try again later.");
        }
    }

    private String hashPassword(String password, byte[] saltBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    private byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }
}
