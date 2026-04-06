package com.example.casestudy.exception.auth;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends AppException {
    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
    }
}
