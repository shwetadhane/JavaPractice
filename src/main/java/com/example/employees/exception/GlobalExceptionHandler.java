package com.example.employees.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleSchemaException(Exception exception, HttpServletRequest request) {
        String message = exception instanceof MethodArgumentNotValidException validationException
                ? validationException.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + " " + error.getDefaultMessage())
                        .collect(Collectors.joining(", "))
                : "Request parameters or body do not match the employee schema";
        return error(HttpStatus.BAD_REQUEST, "SCHEMA_ERROR", message, request);
    }

    @ExceptionHandler(SchemaException.class)
    public ResponseEntity<ApiError> handleSchemaException(
            SchemaException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "SCHEMA_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException exception, HttpServletRequest request) {
        return error(exception.getStatus(), "BUSINESS_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(OperationException.class)
    public ResponseEntity<ApiError> handleOperationException(
            OperationException exception, HttpServletRequest request) {
        return error(exception.getStatus(), "OPERATION_ERROR", exception.getMessage(), request);
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI()));
    }
}
