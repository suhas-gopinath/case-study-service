package com.example.casestudy.service.password;

/**
 * Service interface for password hashing and verification operations.
 * 
 * This interface provides abstraction for password hashing algorithms,
 * allowing different implementations (PBKDF2, BCrypt, etc.) to be used
 * interchangeably without modifying dependent code.
 * 
 * SOLID Principles:
 * - Single Responsibility: Focused solely on password operations
 * - Open/Closed: Open for extension (new algorithms) without modification
 * - Dependency Inversion: Clients depend on this abstraction, not concrete implementations
 * 
 * Future Extensibility:
 * - BCrypt implementation can be added by creating BcryptPasswordService implements PasswordService
 * - No changes required to AuthenticationService or controllers when switching algorithms
 */
public interface PasswordService {
    
    /**
     * Hashes a raw password with a generated salt.
     * 
     * The implementation is responsible for:
     * - Generating a cryptographically secure random salt
     * - Applying the hashing algorithm with appropriate iterations
     * - Encoding the result for storage
     * 
     * @param password The plain text password to hash
     * @param salt The salt bytes to use for hashing
     * @return The hashed password as a Base64-encoded string
     */
    String hash(String password, byte[] salt);
    
    /**
     * Verifies a raw password against a stored hash and salt.
     * 
     * @param rawPassword The plain text password to verify
     * @param storedHash The stored password hash (Base64-encoded)
     * @param storedSalt The stored salt (Base64-encoded)
     * @return true if the password matches, false otherwise
     */
    boolean verify(String rawPassword, String storedHash, String storedSalt);
}