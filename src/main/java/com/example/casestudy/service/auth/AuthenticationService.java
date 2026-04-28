package com.example.casestudy.service.auth;

import com.example.casestudy.model.User;

public interface AuthenticationService {
    
    User register(String username, String password);
    
    User authenticate(String username, String password);
}