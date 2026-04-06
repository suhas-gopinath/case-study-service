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

@Service
public class JwtAccessTokenService implements AccessTokenService {
    
    private final JwtConfig jwtConfig;
    
    public JwtAccessTokenService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }
    
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