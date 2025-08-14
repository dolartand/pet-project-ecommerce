package com.ecommerce.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends ApplicationException {
    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN", HttpStatus.UNAUTHORIZED);
    }
}