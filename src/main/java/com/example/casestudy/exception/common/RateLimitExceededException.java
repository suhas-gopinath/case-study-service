package com.example.casestudy.exception.common;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends AppException {
    
    public RateLimitExceededException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
    
    public RateLimitExceededException() {
        super("Too many requests. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
    }
}