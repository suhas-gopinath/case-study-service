package com.example.casestudy.util;

import com.example.casestudy.exception.TokenValidationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;

import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "m3UjaLTuZyPZL5Hzs7e3UfG7FZT7C8jcDq5B8wXSmFI=";
    private static final long EXPIRATION_TIME = 1800000;

    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static String validateToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
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
            throw new TokenValidationException("JWT token is missing or empty");
        }
    }

}
