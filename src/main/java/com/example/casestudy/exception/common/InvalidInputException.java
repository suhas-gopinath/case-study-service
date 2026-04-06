package com.example.casestudy.exception.common;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidInputException extends AppException {
    public InvalidInputException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
