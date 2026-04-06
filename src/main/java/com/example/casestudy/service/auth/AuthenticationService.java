package com.example.casestudy.service.auth;

import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.model.User;

public interface AuthenticationService {
    
    User register(UserRequest request);
    
    User authenticate(String username, String password);
}