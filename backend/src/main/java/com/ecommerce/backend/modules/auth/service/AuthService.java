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
import com.ecommerce.backend.shared.exception.InvalidCredentialsException;
import com.ecommerce.backend.shared.exception.InvalidTokenException;
import com.ecommerce.backend.shared.exception.UserAlreadyExistsException;
import com.ecommerce.backend.shared.exception.UserNotFoundException;
import com.ecommerce.backend.shared.outbox.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
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
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

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
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    public AuthResponseWrapper<RefreshTokenResponse> refreshToken(String refreshToken) {
        if (!jwtUtils.isTokenValid(refreshToken)) {
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

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        String resetToken = UUID.randomUUID().toString();

        // TODO: Добавить реализацию восстановления пароля

        log.info("Password reset requested for user: {}", user.getEmail());
    }
}