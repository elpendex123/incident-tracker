package com.example.incidenttracker.exception;

/**
 * Exception thrown when validation of input data fails.
 * This should result in a 400 HTTP status code.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
