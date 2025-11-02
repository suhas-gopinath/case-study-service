package com.example.casestudy.util;

import com.example.casestudy.config.JwtConfig;
import com.example.casestudy.exception.TokenValidationException;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private final JwtConfig jwtConfig;

    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationTime()))
                .signWith(SignatureAlgorithm.HS256, jwtConfig.getSecretKey())
                .compact();
    }

    public String validateToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtConfig.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            throw new TokenValidationException("JWT has expired");
        } catch (UnsupportedJwtException e) {
            throw new TokenValidationException("Unsupported JWT token");
        } catch (MalformedJwtException e) {
            throw new TokenValidationException("Malformed JWT token");
        } catch (SignatureException e) {
            throw new TokenValidationException("Invalid JWT signature");
        } catch (IllegalArgumentException e) {
            throw new TokenValidationException("JWT token is missing");
        }
    }
}
