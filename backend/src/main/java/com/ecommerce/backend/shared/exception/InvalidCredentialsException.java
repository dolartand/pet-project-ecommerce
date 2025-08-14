package com.ecommerce.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApplicationException {
    public InvalidCredentialsException(String message) {
        super(message, "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED);
    }
}
