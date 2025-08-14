package com.ecommerce.backend.shared.exception;

import org.springframework.http.HttpStatus;

public abstract class ApplicationException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    protected ApplicationException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected ApplicationException(String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public boolean shouldLogStackTrace() {
        return httpStatus.is5xxServerError();
    }
}
