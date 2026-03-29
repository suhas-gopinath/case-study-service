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

/**
 * Implementation of UserDatabaseService with Resilience4j circuit breaker protection.
 * 
 * This service wraps all UserRepository database calls with circuit breaker pattern
 * to prevent cascading failures when the database becomes unavailable or slow.
 * 
 * SOLID Principles:
 * - Single Responsibility: Handles only database operations with resilience
 * - Open/Closed: Can be extended without modifying existing code
 * - Liskov Substitution: Fully substitutable for UserDatabaseService interface
 * - Dependency Inversion: Depends on UserRepository abstraction
 * 
 * Circuit Breaker Configuration:
 * - Name: userDatabase
 * - Fallback methods handle circuit open state
 * - Timeout exceptions wrapped in DatabaseTimeoutException
 * 
 * @see UserDatabaseService
 * @see com.example.casestudy.repository.UserRepository
 */
@Service
public class UserDatabaseServiceImpl implements UserDatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(UserDatabaseServiceImpl.class);
    private static final String CIRCUIT_BREAKER_NAME = "userDatabase";

    private final UserRepository userRepository;

    public UserDatabaseServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds a user by username with circuit breaker protection.
     * 
     * Circuit Breaker Behavior:
     * - Monitors database call success/failure rate
     * - Opens circuit when failure threshold exceeded
     * - Calls fallback method when circuit is open
     * 
     * @param username The username to search for
     * @return Optional containing the User if found, empty otherwise
     * @throws DatabaseCircuitOpenException if circuit is open
     * @throws DatabaseTimeoutException if operation times out
     */
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

    /**
     * Fallback method for findByUsername when circuit breaker is open.
     * 
     * @param username The username parameter from original call
     * @param throwable The exception that triggered the fallback
     * @return Never returns, always throws DatabaseCircuitOpenException
     * @throws DatabaseCircuitOpenException always
     */
    private Optional<User> findByUsernameFallback(String username, Throwable throwable) {
        logger.warn("Circuit breaker OPEN for findByUsername. Username: {}. Reason: {}", 
                    username, throwable.getMessage());
        throw new DatabaseCircuitOpenException();
    }

    /**
     * Saves a user entity to the database with circuit breaker protection.
     * 
     * Circuit Breaker Behavior:
     * - Monitors database call success/failure rate
     * - Opens circuit when failure threshold exceeded
     * - Calls fallback method when circuit is open
     * 
     * @param user The user entity to save
     * @return The saved user entity
     * @throws DatabaseCircuitOpenException if circuit is open
     * @throws DatabaseTimeoutException if operation times out
     */
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

    /**
     * Fallback method for save when circuit breaker is open.
     * 
     * @param user The user parameter from original call
     * @param throwable The exception that triggered the fallback
     * @return Never returns, always throws DatabaseCircuitOpenException
     * @throws DatabaseCircuitOpenException always
     */
    private User saveFallback(User user, Throwable throwable) {
        logger.warn("Circuit breaker OPEN for save. Username: {}. Reason: {}", 
                    user.getUsername(), throwable.getMessage());
        throw new DatabaseCircuitOpenException();
    }
}
