package com.example.casestudy.security;

public interface AccessTokenService {
    String generateToken(String username);
    String parseAndExtractUsername(String token);
}
