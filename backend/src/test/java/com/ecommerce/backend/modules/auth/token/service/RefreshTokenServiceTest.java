package com.ecommerce.backend.modules.auth.token.service;

import com.ecommerce.backend.modules.auth.jwt.JwtProps;
import com.ecommerce.backend.modules.auth.token.entity.RefreshToken;
import com.ecommerce.backend.modules.auth.token.repository.RefreshTokenRepository;
import com.ecommerce.backend.modules.user.entity.User;
import com.ecommerce.backend.modules.user.repository.UserRepository;
import com.ecommerce.backend.shared.exception.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtProps jwtProps;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        refreshToken = RefreshToken.builder()
                .id(100L)
                .token("test-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void saveRefreshToken_shouldSaveAndCacheToken() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtProps.getRefreshTokenExpiration()).thenReturn(86400000L); // 1 day
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        RefreshToken savedToken = refreshTokenService.saveRefreshToken("test-token", 1L);

        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo("test-token");
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        verify(redisTemplate.opsForValue(), times(1)).set(anyString(), anyString(), any());
    }

    @Test
    void isTokenValid_whenTokenIsValidInDbAndCache_shouldReturnTrue() {
        when(refreshTokenRepository.findValidToken("test-token")).thenReturn(Optional.of(refreshToken));
        when(valueOperations.get("refresh_token:1")).thenReturn("test-token");

        boolean isValid = refreshTokenService.isTokenValid("test-token");

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_whenTokenNotInDb_shouldReturnFalse() {
        when(refreshTokenRepository.findValidToken("test-token")).thenReturn(Optional.empty());

        boolean isValid = refreshTokenService.isTokenValid("test-token");

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_whenTokenNotInCache_shouldReturnFalse() {
        when(refreshTokenRepository.findValidToken("test-token")).thenReturn(Optional.of(refreshToken));
        when(valueOperations.get("refresh_token:1")).thenReturn(null);

        boolean isValid = refreshTokenService.isTokenValid("test-token");

        assertThat(isValid).isFalse();
    }

    @Test
    void findByToken_whenTokenIsValid_shouldReturnToken() {
        when(refreshTokenRepository.findValidToken("test-token")).thenReturn(Optional.of(refreshToken));

        RefreshToken foundToken = refreshTokenService.findByToken("test-token");

        assertThat(foundToken).isEqualTo(refreshToken);
    }

    @Test
    void findByToken_whenTokenIsInvalid_shouldThrowException() {
        when(refreshTokenRepository.findValidToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.findByToken("invalid-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void revokeToken_shouldRevokeFromDbAndCache() {
        when(refreshTokenRepository.findValidToken("test-token")).thenReturn(Optional.of(refreshToken));

        refreshTokenService.revokeToken("test-token");

        verify(refreshTokenRepository, times(1)).revokeToken("test-token");
        verify(redisTemplate, times(1)).delete("refresh_token:1");
    }

    @Test
    void revokeAllUserTokens_shouldRevokeAllFromDbAndCache() {
        refreshTokenService.revokeAllUserTokens(1L);

        verify(refreshTokenRepository, times(1)).revokeAllUserTokens(1L);
        verify(redisTemplate, times(1)).delete("refresh_token:1");
    }
}
