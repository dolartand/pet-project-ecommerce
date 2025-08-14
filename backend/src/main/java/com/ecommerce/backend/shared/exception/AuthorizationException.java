package com.ecommerce.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends ApplicationException {
    public AuthorizationException(String message) {
        super(message, "ACCESS_DENIED", HttpStatus.FORBIDDEN);
    }
}
