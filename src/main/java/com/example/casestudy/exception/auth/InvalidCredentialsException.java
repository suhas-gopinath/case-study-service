package com.example.casestudy.exception.auth;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AppException {

    /**
     * Constructs a new InvalidCredentialsException with a predefined message
     * and HTTP status UNAUTHORIZED (401).
     */
    public InvalidCredentialsException() {
        super("Invalid username or password", HttpStatus.UNAUTHORIZED);
    }
}
