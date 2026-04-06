package com.example.casestudy.exception.common;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends AppException {
    public RateLimitExceededException() {
        super("Rate limit exceeded. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
    }
}
