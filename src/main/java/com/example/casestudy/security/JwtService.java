package com.example.casestudy.security;

import com.example.casestudy.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService implements AccessTokenService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(JwtConfig jwtConfig) {
        this.key = Keys.hmacShaKeyFor(
                jwtConfig.getSecretKey().getBytes(StandardCharsets.UTF_8)
        );
        this.expirationMillis = jwtConfig.getExpirationTime();
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String parseAndExtractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}