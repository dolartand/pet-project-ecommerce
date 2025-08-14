package com.ecommerce.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class SystemException extends ApplicationException {
    public SystemException(String message) {
        super(message, "SYSTEM_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public SystemException(String message, Throwable cause) {
        super(message, "SYSTEM_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public SystemException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
