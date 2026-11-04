package com.example.employees.exception;

import org.springframework.http.HttpStatus;

public class OperationException extends RuntimeException {
    private final HttpStatus status;

    public OperationException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
