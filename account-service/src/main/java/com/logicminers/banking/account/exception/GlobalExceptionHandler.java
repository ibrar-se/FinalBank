package com.logicminers.banking.account.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j // Provides clean logging capabilities
public class GlobalExceptionHandler {

    /**
     * Catches when an account or resource isn't found.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found execution at path [{}]: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(),
                LocalDateTime.now(),
                null
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Catches business rule violations (like insufficient funds).
     * Returns 422 Unprocessable Entity (or 400 Bad Request).
     */
    @ExceptionHandler(AccountBusinessException.class)
    public ResponseEntity<ErrorResponse> handleAccountBusiness(AccountBusinessException ex, HttpServletRequest request) {
        log.error("Banking business rule violation [{}] at path [{}]: {}", ex.getErrorCode(), request.getRequestURI(), ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                request.getRequestURI(),
                LocalDateTime.now(),
                null
        );
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Catches automatic `@Valid` request input payload validation failures.
     * Returns 400 Bad Request with precise field failures.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationFailures(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Input validation failed at path [{}]: {}", request.getRequestURI(), errors);

        ErrorResponse error = new ErrorResponse(
                "VALIDATION_FAILED",
                "Provided request body contains invalid data.",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                LocalDateTime.now(),
                errors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Global Catch-All for unexpected infrastructure or code crashes.
     * Returns 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        // Critical: Log the full stack trace here for developer debugging
        log.error("CRITICAL: Unexpected systemic failure at path [{}]", request.getRequestURI(), ex);

        ErrorResponse error = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected system error occurred. Please contact backend support.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI(),
                LocalDateTime.now(),
                null
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}