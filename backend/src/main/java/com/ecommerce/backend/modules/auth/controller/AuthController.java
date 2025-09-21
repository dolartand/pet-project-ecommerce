package com.ecommerce.backend.modules.auth.controller;

import com.ecommerce.backend.modules.auth.service.AuthService;
import com.ecommerce.backend.shared.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());
        authService.register(request);

        Map<String, String> response = Map.of("message", "Пользователь успешно зарегистрирован.");
        log.info("User registered successfully with email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Logging user with email: {}", request.getEmail());
        AuthResponseWrapper<LoginResponse> wrapper = authService.login(request);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", wrapper.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .build();

        log.info("User logged in successfully with email: {}", request.getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(wrapper.getResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@CookieValue(name = "refreshToken") String refreshToken) {
        log.info("Refreshing token");
        AuthResponseWrapper<RefreshTokenResponse> wrapper = authService.refreshToken(refreshToken);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", wrapper.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(wrapper.getResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken") String refreshToken,
                                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Logging out user with id: {}", userId);
        authService.logout(refreshToken, userId);
        log.info("User logged out successfully with id: {}", userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Requesting password reset for email: {}", request.getEmail());
        ForgotPasswordResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(@RequestParam String token) {
        log.info("Validating reset token");
        boolean isValid = authService.validateResetToken(token);

        Map<String, Boolean> response = Map.of("valid", isValid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Resetting password with token");
        authService.resetPassword(request);

        Map<String, String> response = Map.of(
                "message", "Password has been changed successfully. Use new password for sign in"
        );
        return ResponseEntity.ok(response);
    }
}