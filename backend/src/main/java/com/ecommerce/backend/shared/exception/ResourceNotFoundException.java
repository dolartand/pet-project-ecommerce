package com.ecommerce.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApplicationException {
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    public  ResourceNotFoundException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.NOT_FOUND);
    }

    public static ResourceNotFoundException product(Long id) {
        return new ResourceNotFoundException("Товар с ID " + id + " не найден");
    }

    public static ResourceNotFoundException category(Long id) {
        return new ResourceNotFoundException("Категория с ID " + id + " не найден");
    }
}
