package com.ecommerce.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApplicationException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }
}
