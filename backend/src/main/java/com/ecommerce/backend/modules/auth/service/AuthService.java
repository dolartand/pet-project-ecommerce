package com.ecommerce.backend.modules.auth.service;

import com.ecommerce.backend.modules.auth.jwt.CustomUserDetails;
import com.ecommerce.backend.modules.auth.jwt.CustomUserDetailsService;
import com.ecommerce.backend.modules.auth.jwt.JwtUtils;
import com.ecommerce.backend.modules.user.entity.User;
import com.ecommerce.backend.modules.user.repository.UserRepository;
import com.ecommerce.backend.shared.dto.*;
import com.ecommerce.backend.shared.exception.InvalidCredentialsException;
import com.ecommerce.backend.shared.exception.InvalidTokenException;
import com.ecommerce.backend.shared.exception.UserAlreadyExistsException;
import com.ecommerce.backend.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    public void register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail)) {
            throw new UserAlreadyExistsException("User with email: " + req.getEmail() + " already exists");
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
        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail()));
        log.info("User registered successfully: {}", savedUser.getEmail());
    }

    public LoginResponse login(LoginRequest request) {
        try {
            // Аутентифицируем пользователя
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Генерируем токены
            String accessToken = jwtUtils.generateAccessToken(userDetails, userDetails.getId());
            String refreshToken = jwtUtils.generateRefreshToken(userDetails, userDetails.getId());

            // Преобразуем в DTO
            UserDto userDto = UserDto.builder()
                    .id(userDetails.getId())
                    .email(userDetails.getEmail())
                    .firstName(userDetails.getFirstName())
                    .lastName(userDetails.getLastName())
                    .role(userDetails.getRole())
                    .createdAt(LocalDateTime.now()) // В реальном приложении получить из БД
                    .updatedAt(LocalDateTime.now())
                    .build();

            log.info("User logged in successfully: {}", userDetails.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(userDto)
                    .build();

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtils.isTokenValid(refreshToken)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String username = jwtUtils.getUsernameFromToken(refreshToken);
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Выполняем rotation refresh токена
        String newAccessToken = jwtUtils.generateAccessToken(userDetails, userId);
        String newRefreshToken = jwtUtils.rotateRefreshToken(refreshToken, userDetails, userId);

        log.info("Tokens refreshed successfully for user: {}", username);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(LogoutRequest request, Long userId) {
        String refreshToken = request.getRefreshToken();

        // Добавляем refresh token в черный список
        jwtUtils.blacklistToken(refreshToken);

        // Отзываем refresh token из Redis
        jwtUtils.revokeRefreshToken(userId);

        log.info("User logged out successfully: userId={}", userId);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        String resetToken = UUID.randomUUID().toString();

        // В реальном приложении сохранили бы токен в БД с временем жизни
        // и отправили бы email с ссылкой для сброса

        eventPublisher.publishEvent(new PasswordResetRequestedEvent(user.getId(), user.getEmail(), resetToken));

        log.info("Password reset requested for user: {}", user.getEmail());
    }

}
