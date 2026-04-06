package com.example.casestudy.exception.database;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

public class DatabaseCircuitOpenException extends AppException {
    public DatabaseCircuitOpenException() {
        super("Database service is temporarily unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
