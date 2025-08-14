package com.ecommerce.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApplicationException {
    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
