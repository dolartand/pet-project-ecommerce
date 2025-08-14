package com.ecommerce.backend.shared.exception;

import com.ecommerce.backend.shared.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex, HttpServletRequest request) {
        if (ex.shouldLogStackTrace()) {
            log.error("Application error: {} at {} {}", ex.getMessage(), request.getMethod(), request.getRequestURI(), ex);
        } else {
            log.warn("Client error: {} at {} {}", ex.getMessage(), request.getMethod(), request.getRequestURI());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .build();
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed for request {} {}: {} errors", request.getMethod(), request.getRequestURI(), ex.getErrorCount());

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.validation(
                "VALIDATION_FAILED",
                "Validation exception for request fields",
                validationErrors
        );

        errorResponse.setPath(request.getRequestURI());
        errorResponse.setMethod(request.getMethod());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint validation at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());

        Map<String, String> validationErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));
        ErrorResponse errorResponse = ErrorResponse.validation(
                "CONSTRAINT_VIOLATION",
                "Violation of validation constraints",
                validationErrors
        );

        errorResponse.setPath(request.getRequestURI());
        errorResponse.setMethod(request.getMethod());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed JSON request at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.simple(
                "MALFORMED_JSON",
                "Incorrect JSON format in body"
        );

        errorResponse.setPath(request.getRequestURI());
        errorResponse.setMethod(request.getMethod());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("Missing required parameter '{}' at {} {}", ex.getParameterName(), request.getMethod(), request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.simple(
                "MISSING_PARAMETER",
                String.format("Missed required parameter: %s", ex.getParameterName())
        );

        errorResponse.setPath(request.getRequestURI());
        errorResponse.setMethod(request.getMethod());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Type mismatch for parameter '{}' at {} {}: expected {}, got '{}'",
                ex.getName(), request.getMethod(), request.getRequestURI(),
                ex.getRequiredType().getSimpleName(), ex.getValue());

        String message = String.format("Incorrect parameter type '%s'. Expected: %s",
                ex.getName(), ex.getRequiredType().getSimpleName());

        ErrorResponse errorResponse = ErrorResponse.simple("PARAMETER_TYPE_MISMATCH", message);
        errorResponse.setPath(request.getRequestURI());
        errorResponse.setMethod(request.getMethod());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("Method not supported: {} for {}", ex.getMethod(), request.getRequestURI());

        String message = String.format("HTTP method %s is not supported for this endpoint", ex.getMethod());

        ErrorResponse errorResponse = ErrorResponse.simple("METHOD_NOT_SUPPORTED", message);
        errorResponse.setPath(request.getRequestURI());
        errorResponse.setMethod(request.getMethod());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {

        log.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());

        ErrorResponse errorResponse = ErrorResponse.simple(
                "ENDPOINT_NOT_FOUND",
                "The requested endpoint was not found"
        );

        errorResponse.setPath(request.getRequestURI());
        errorResponse.setMethod(request.getMethod());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {

        log.warn("Authentication failed at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.simple(
                "AUTHENTICATION_FAILED",
                "Authentication failed"
        );

        errorResponse.setPath(request.getRequestURI());
        errorResponse.setMethod(request.getMethod());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.simple(
                "ACCESS_DENIED",
                "Not enough rights to perform the operation"
        );

        errorResponse.setPath(request.getRequestURI());
        errorResponse.setMethod(request.getMethod());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error at {} {}", request.getMethod(), request.getRequestURI(), ex);

        ErrorResponse errorResponse = ErrorResponse.simple(
                "INTERNAL_SERVER_ERROR",
                "Internal server error"
        );

        errorResponse.setPath(request.getRequestURI());
        errorResponse.setMethod(request.getMethod());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
