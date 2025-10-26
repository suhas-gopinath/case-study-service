package com.example.CaseStudy.service;
import com.example.CaseStudy.model.User;
import com.example.CaseStudy.repository.UserRepository;
import com.example.CaseStudy.dto.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;

@Service
public class UserService {

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            System.err.println("Username already exists: " + request.getUsername());
            return null;
        }

        try {
            byte[] saltBytes = generateSalt();
            String saltString = Base64.getEncoder().encodeToString(saltBytes);
            String hashedPassword = hashPassword(request.getPassword(), saltBytes);

            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setPasswordHash(hashedPassword);
            newUser.setSalt(saltString);
            return userRepository.save(newUser);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Error during password hashing" + e.getMessage());
            return null;
        }
    }

    public Optional<User> authenticateUser(String username, String rawPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            try {
                byte[] storedSaltBytes = Base64.getDecoder().decode(user.getSalt());
                String hashedAttemptedPassword = hashPassword(rawPassword, storedSaltBytes);

                if (hashedAttemptedPassword.equals(user.getPasswordHash())) {
                    return Optional.of(user);
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
                System.err.println("Error during authentication for user " + username + ": " + e.getMessage());
            }
        }
        return Optional.empty();
    }


    public String hashPassword(String password, byte[] saltBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    public byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }
}