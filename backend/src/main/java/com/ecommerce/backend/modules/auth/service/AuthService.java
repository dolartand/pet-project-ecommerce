package com.ecommerce.backend.modules.auth.service;

import com.ecommerce.backend.config.RabbitConfig;
import com.ecommerce.backend.modules.auth.jwt.CustomUserDetails;
import com.ecommerce.backend.modules.auth.jwt.CustomUserDetailsService;
import com.ecommerce.backend.modules.auth.jwt.JwtUtils;
import com.ecommerce.backend.modules.auth.token.service.RefreshTokenService;
import com.ecommerce.backend.modules.user.entity.User;
import com.ecommerce.backend.modules.user.entity.UserRole;
import com.ecommerce.backend.modules.user.repository.UserRepository;
import com.ecommerce.backend.shared.dto.*;
import com.ecommerce.backend.shared.events.UserRegisteredEvent;
import com.ecommerce.backend.shared.exception.*;
import com.ecommerce.backend.shared.outbox.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RabbitTemplate rabbitTemplate;
    private final EventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${password.reset.token.expiration:3600}")
    private long resetTokenExpiration;

    @Value("${password.reset.max-attempts:3}")
    private int maxResetAttempts;

    public void register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed. Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("User with email: " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        UserRegisteredEvent event = new UserRegisteredEvent(savedUser);
        eventPublisher.publish(event, RabbitConfig.USER_EVENTS_EXCHANGE, "user.registered");

        log.info("User registered successfully: {}", savedUser.getEmail());
    }

    public AuthResponseWrapper<LoginResponse> login(LoginRequest request) {
        log.info("Attempting to log in user with email: {}", request.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            log.info("User authenticated successfully: {}", request.getEmail());

            String accessToken = jwtUtils.generateAccessToken(userDetails, userDetails.getId());
            String refreshToken = jwtUtils.generateRefreshToken(userDetails, userDetails.getId());

            UserDto userDto = UserDto.builder()
                    .id(userDetails.getId())
                    .email(userDetails.getEmail())
                    .firstName(userDetails.getFirstName())
                    .lastName(userDetails.getLastName())
                    .role(userDetails.getRole())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            log.info("User logged in successfully: {}", userDetails.getEmail());

            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .user(userDto)
                    .build();

            return AuthResponseWrapper.<LoginResponse>builder()
                    .refreshToken(refreshToken)
                    .response(response)
                    .build();

        } catch (BadCredentialsException e) {
            log.error("Login failed. Invalid credentials for user with email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    public AuthResponseWrapper<RefreshTokenResponse> refreshToken(String refreshToken) {
        log.info("Attempting to refresh token");
        if (!jwtUtils.isTokenValid(refreshToken)) {
            log.error("Refresh token failed. Invalid or expired token");
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String username = jwtUtils.getUsernameFromToken(refreshToken);
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccessToken = jwtUtils.generateAccessToken(userDetails, userId);
        String newRefreshToken = jwtUtils.rotateRefreshToken(refreshToken, userDetails, userId);

        log.info("Tokens refreshed successfully for user: {}", username);

        RefreshTokenResponse refreshTokenResponse = RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .build();

        return AuthResponseWrapper.<RefreshTokenResponse>builder()
                .refreshToken(newRefreshToken)
                .response(refreshTokenResponse)
                .build();
    }

    public void logout(String refreshToken, Long userId) {
        refreshTokenService.revokeToken(refreshToken);

        log.info("User logged out successfully: userId={}", userId);
    }

    /**
     * Для проекта возвращаем токен в ответе
     */
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());

        String rateLimitKey = "password_reset_attempts:" + request.getEmail();
        String attempts = redisTemplate.opsForValue().get(rateLimitKey);

        if (attempts != null && Integer.parseInt(attempts) >= maxResetAttempts) {
            log.warn("Too many password reset attempts for email: {}", request.getEmail());
            throw new TooManyAttemptsException("Too much attempts. Try again later.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        String resetToken = UUID.randomUUID().toString();

        String tokenKey = "password_reset_token:" + resetToken;
        String userIdValue = String.valueOf(user.getId());

        redisTemplate.opsForValue().set(
                tokenKey,
                userIdValue,
                Duration.ofSeconds(resetTokenExpiration)
        );

        redisTemplate.opsForValue().increment(rateLimitKey);
        redisTemplate.expire(rateLimitKey, 24, TimeUnit.HOURS);

        String userTokenKey = "user_reset_token:" + user.getId();
        redisTemplate.opsForValue().set(
                userTokenKey,
                resetToken,
                Duration.ofSeconds(resetTokenExpiration)
        );

        log.info("Password reset token generated for user: {}", user.getEmail());

        return ForgotPasswordResponse.builder()
                .message("Token for reset password created")
                .token(resetToken)
                .build();
    }

    public boolean validateResetToken(String token) {
        String tokenKey = "password_reset_token:" + token;
        return redisTemplate.hasKey(tokenKey);
    }

    public void resetPassword(ResetPasswordRequest req) {
        log.info("Attempting for reset password");

        if (req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new ValidationException("Passwords are not match");
        }

        String tokenKey = "password_reset_token:" + req.getToken();
        String userIdStr = redisTemplate.opsForValue().get(tokenKey);

        if (userIdStr == null) {
            log.error("Invalid or expired reset token");
            throw new InvalidTokenException("Reset token is not valid or expired");
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User is not found"));

        if (passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw new ValidationException("The new password must be different from the current one");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        redisTemplate.delete(tokenKey);

        String userTokenKey = "user_reset_token:" + userId;
        redisTemplate.delete(userTokenKey);

        refreshTokenService.revokeAllUserTokens(userId);

        log.info("Password successfully reset for user: {}", user.getEmail());
    }
}