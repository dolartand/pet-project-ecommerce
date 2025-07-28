package com.ecommerce.backend.modules.auth.token.service;

import com.ecommerce.backend.modules.auth.jwt.JwtProps;
import com.ecommerce.backend.modules.auth.token.entity.RefreshToken;
import com.ecommerce.backend.modules.auth.token.repository.RefreshTokenRepository;
import com.ecommerce.backend.modules.user.entity.User;
import com.ecommerce.backend.modules.user.repository.UserRepository;
import com.ecommerce.backend.shared.exception.InvalidTokenException;
import com.ecommerce.backend.shared.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProps jwtProps;
    private final RedisTemplate<String, String> redisTemplate;

    public RefreshToken saveRefreshToken(String tokenValue, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProps.getRefreshTokenExpiration() / 1000))
                .build();

        RefreshToken saved =  refreshTokenRepository.save(token);

        String redisKey = "refresh_token:" + userId;
        redisTemplate.opsForValue().set(
                redisKey,
                tokenValue,
                Duration.ofMillis(jwtProps.getRefreshTokenExpiration())
        );

        log.info("Refresh token saved for user: {}", userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public boolean isTokenValid(String tokenValue) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findValidToken(tokenValue);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        RefreshToken token = tokenOpt.get();

        String redisKey = "refreshToken:" + token.getUser().getId();
        String redisToken = redisTemplate.opsForValue().get(redisKey);

        return token.isValid() && tokenValue.equals(redisToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken findByToken(String tokenValue) {
        return refreshTokenRepository.findValidToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));
    }

    public void revokeToken(String tokenValue) {
        refreshTokenRepository.revokeToken(tokenValue);

        Optional<RefreshToken> token = refreshTokenRepository.findValidToken(tokenValue);
        if (token.isPresent()) {
            Long userId = token.get().getUser().getId();
            String redisKey = "refresh_token:" + userId;
            redisTemplate.delete(redisKey);
        }
        log.info("Refresh token revoked: {}", tokenValue.substring(0, 10) + "...");
    }

    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);

        String redisKey = "refreshToken:" + userId;
        redisTemplate.delete(redisKey);

        log.info("All refresh tokens revoked for user: {}", userId);
    }

    public RefreshToken rotateToken(String oldTokenValue, String newTokenValue) {
        RefreshToken oldToken = findByToken(oldTokenValue);
        Long userId = oldToken.getUser().getId();
        revokeToken(oldTokenValue);
        return saveRefreshToken(newTokenValue, userId);
    }
}
