package com.example.casestudy.exception.database;

import com.example.casestudy.exception.AppException;
import org.springframework.http.HttpStatus;

public class DatabaseTimeoutException extends AppException {
    public DatabaseTimeoutException() {
        super("Database operation timed out", HttpStatus.GATEWAY_TIMEOUT);
    }
}
