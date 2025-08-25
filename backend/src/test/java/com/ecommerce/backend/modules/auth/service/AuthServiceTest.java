package com.ecommerce.backend.modules.auth.service;

import com.ecommerce.backend.modules.auth.jwt.CustomUserDetails;
import com.ecommerce.backend.modules.auth.jwt.CustomUserDetailsService;
import com.ecommerce.backend.modules.auth.jwt.JwtUtils;
import com.ecommerce.backend.modules.auth.token.entity.RefreshToken;
import com.ecommerce.backend.modules.auth.token.service.RefreshTokenService;
import com.ecommerce.backend.modules.user.entity.User;
import com.ecommerce.backend.modules.user.entity.UserRole;
import com.ecommerce.backend.modules.user.repository.UserRepository;
import com.ecommerce.backend.modules.user.sevice.UserService;
import com.ecommerce.backend.shared.dto.*;
import com.ecommerce.backend.shared.exception.InvalidCredentialsException;
import com.ecommerce.backend.shared.exception.UserAlreadyExistsException;
import com.ecommerce.backend.shared.outbox.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private CustomUserDetailsService customUserDetailsService;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    public void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("email");
        registerRequest.setPassword("password");
        registerRequest.setFirstName("firstName");
        registerRequest.setLastName("lastName");

        user = User.builder()
                .id(1L)
                .email("email")
                .passwordHash("password")
                .firstName("firstName")
                .lastName("lastName")
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void registerUser_whenUserDoesNotExist_shouldRegisterUser() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.register(registerRequest);

        verify(userRepository, times(1)).save(any(User.class));
        verify(eventPublisher, times(1)).publish(any(), anyString(), anyString());
    }

    @Test
    void registerUser_whenUserExists_shouldThrowUserAlreadyExistsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_whenCredentialsAreValid_shouldReturnLoginResponse() {
        LoginRequest loginRequest = new LoginRequest("email", "password");
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails customUserDetails = CustomUserDetails.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(jwtUtils.generateAccessToken(any(), anyLong())).thenReturn("accessToken");
        when(jwtUtils.generateRefreshToken(any(), anyLong())).thenReturn("refreshToken");

        LoginResponse result = authService.login(loginRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("accessToken");
        assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(result.getUser().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void login_whenCredentialsAreInvalid_shouldThrowInvalidCredentialsException() {
        LoginRequest loginRequest = new LoginRequest("email", "wrongPassword");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad Credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refreshToken_shouldReturnNewTokens() {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refreshToken");
        UserDetails userDetails = CustomUserDetails.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .build();

        when(jwtUtils.isTokenValid("refreshToken")).thenReturn(true);
        when(jwtUtils.getUsernameFromToken("refreshToken")).thenReturn("email");
        when(jwtUtils.getUserIdFromToken("refreshToken")).thenReturn(1L);
        when(customUserDetailsService.loadUserByUsername("email")).thenReturn(userDetails);
        when(jwtUtils.rotateRefreshToken(anyString(), any(), anyLong())).thenReturn("newRefreshToken");
        when(jwtUtils.generateAccessToken(any(), anyLong())).thenReturn("newAccessToken");

        RefreshTokenResponse result = authService.refreshToken(refreshTokenRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(result.getRefreshToken()).isEqualTo("newRefreshToken");
    }

    @Test
    void logout_shouldRevokeToken() {
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken("refreshToken");

        authService.logout(logoutRequest, user.getId());

        verify(refreshTokenService, times(1)).revokeToken("refreshToken");
    }
}
