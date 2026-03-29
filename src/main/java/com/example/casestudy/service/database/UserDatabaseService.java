package com.example.casestudy.service.database;

import com.example.casestudy.model.User;

import java.util.Optional;

/**
 * Service interface for database operations on User entities with circuit breaker support.
 * 
 * This interface defines the contract for database access operations that are protected
 * by the Resilience4j circuit breaker pattern. It provides an abstraction layer between
 * the business logic and the data access layer.
 * 
 * Design Principles:
 * - Single Responsibility: Handles only database operations for User entities
 * - Dependency Inversion: Provides abstraction for database access
 * - Interface Segregation: Contains only essential database operations
 * 
 * Circuit Breaker Pattern:
 * Implementations of this interface should apply circuit breaker protection to prevent
 * cascading failures when the database becomes unavailable or slow.
 * 
 * @see com.example.casestudy.repository.UserRepository
 */
public interface UserDatabaseService {

    /**
     * Finds a user by username with circuit breaker protection.
     * 
     * @param username The username to search for
     * @return Optional containing the User if found, empty otherwise
     * @throws com.example.casestudy.exception.database.DatabaseCircuitOpenException if circuit is open
     * @throws com.example.casestudy.exception.database.DatabaseTimeoutException if operation times out
     */
    Optional<User> findByUsername(String username);

    /**
     * Saves a user entity to the database with circuit breaker protection.
     * 
     * @param user The user entity to save
     * @return The saved user entity
     * @throws com.example.casestudy.exception.database.DatabaseCircuitOpenException if circuit is open
     * @throws com.example.casestudy.exception.database.DatabaseTimeoutException if operation times out
     */
    User save(User user);
}
