package com.example.casestudy.service.token;


public interface AccessTokenService {

    String generateAccessToken(String username);
    
    String validateAccessToken(String token);
}