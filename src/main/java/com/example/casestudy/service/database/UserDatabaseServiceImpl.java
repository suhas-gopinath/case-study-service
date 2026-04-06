package com.example.casestudy.service.database;

import com.example.casestudy.exception.database.DatabaseCircuitOpenException;
import com.example.casestudy.exception.database.DatabaseTimeoutException;
import com.example.casestudy.model.User;
import com.example.casestudy.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserDatabaseServiceImpl implements UserDatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(UserDatabaseServiceImpl.class);
    private static final String CIRCUIT_BREAKER_NAME = "userDatabase";

    private final UserRepository userRepository;

    public UserDatabaseServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "findByUsernameFallback")
    public Optional<User> findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        try {
            Optional<User> user = userRepository.findByUsername(username);
            logger.debug("User lookup completed for username: {}", username);
            return user;
        } catch (DataAccessException e) {
            logger.error("Database access error while finding user: {}", username, e);
            throw new DatabaseTimeoutException();
        }
    }

    @SuppressWarnings("unused")
    private Optional<User> findByUsernameFallback(String username, Throwable throwable) {
        logger.warn("Circuit breaker OPEN for findByUsername. Username: {}. Reason: {}", 
                    username, throwable.getMessage());
        throw new DatabaseCircuitOpenException();
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "saveFallback")
    public User save(User user) {
        logger.debug("Saving user: {}", user.getUsername());
        try {
            User savedUser = userRepository.save(user);
            logger.debug("User saved successfully: {}", savedUser.getUsername());
            return savedUser;
        } catch (DataAccessException e) {
            logger.error("Database access error while saving user: {}", user.getUsername(), e);
            throw new DatabaseTimeoutException();
        }
    }

    @SuppressWarnings("unused")
    private User saveFallback(User user, Throwable throwable) {
        logger.warn("Circuit breaker OPEN for save. Username: {}. Reason: {}", 
                    user.getUsername(), throwable.getMessage());
        throw new DatabaseCircuitOpenException();
    }
}
