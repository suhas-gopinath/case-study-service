package com.example.casestudy.service.token;

public interface RefreshTokenService {
    
    String createRefreshToken(String username);
    
    String validateRefreshToken(String token);

    void revokeRefreshToken(String token);
}
