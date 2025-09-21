package com.ecommerce.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class TooManyAttemptsException extends ApplicationException {
    public TooManyAttemptsException(String message) {
        super(message, "TOO_MANY_ATTEMPTS_ERROR", HttpStatus.TOO_MANY_REQUESTS);
    }
}
