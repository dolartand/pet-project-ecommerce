package com.ecommerce.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends ApplicationException {
    public BusinessException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, HttpStatus.BAD_REQUEST, cause);
    }
}
