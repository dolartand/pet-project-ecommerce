package com.ecommerce.backend.shared.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException product(Long id) {
        return new ResourceNotFoundException("Товар с ID " + id + " не найден");
    }

    public static ResourceNotFoundException category(Long id) {
        return new ResourceNotFoundException("Категория с ID " + id + " не найден");
    }
}
