package com.example.casestudy.service.token;

import com.example.casestudy.config.JwtConfig;
import com.example.casestudy.exception.auth.TokenValidationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT implementation of the TokenService interface.
 * 
 * This implementation handles JWT token generation and validation directly,
 * providing a clean abstraction layer for future extensibility.
 * 
 * SOLID Principles:
 * - Single Responsibility: Handles only JWT token operations
 * - Open/Closed: Can be extended or replaced without modifying clients
 * - Liskov Substitution: Fully substitutable for TokenService interface
 * - Dependency Inversion: Depends on JwtConfig abstraction
 * 
 * Design Notes:
 * - Contains all JWT logic directly for better encapsulation
 * - Future refresh token logic will be added to a separate RefreshTokenService
 * 
 * Future Extensibility:
 * - When adding refresh tokens, create RefreshTokenService without modifying this class
 * - Multiple token services can coexist for different token types
 */
@Service
public class JwtTokenService implements TokenService {
    
    private final JwtConfig jwtConfig;
    
    public JwtTokenService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }
    
    /**
     * Generates a JWT access token for the specified username.
     * 
     * @param username The username to encode in the JWT
     * @return The generated JWT access token
     */
    @Override
    public String generateAccessToken(String username) {
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationTime()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Validates a JWT access token and extracts the username.
     * 
     * @param token The JWT access token to validate
     * @return The username extracted from the JWT
     * @throws com.example.casestudy.exception.TokenValidationException if token is invalid
     */
    @Override
    public String validateAccessToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            throw new TokenValidationException("JWT has expired");
        } catch (UnsupportedJwtException e) {
            throw new TokenValidationException("Unsupported JWT token");
        } catch (MalformedJwtException e) {
            throw new TokenValidationException("Malformed JWT token");
        } catch (SecurityException e) {
            throw new TokenValidationException("Invalid JWT signature");
        } catch (IllegalArgumentException e) {
            throw new TokenValidationException("JWT token is missing");
        }
    }
}