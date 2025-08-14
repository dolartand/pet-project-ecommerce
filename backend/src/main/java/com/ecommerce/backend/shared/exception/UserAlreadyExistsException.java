package com.ecommerce.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends ApplicationException {
    public UserAlreadyExistsException(String message) {
        super(message, "USER_ALREADY_EXISTS", HttpStatus.CONFLICT);
    }
}