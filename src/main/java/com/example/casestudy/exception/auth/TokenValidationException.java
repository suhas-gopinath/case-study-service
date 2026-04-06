package com.example.casestudy.exception.auth;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

public class TokenValidationException extends AppException {
    public TokenValidationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
