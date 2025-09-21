package com.ecommerce.backend.modules.user.service;

import com.ecommerce.backend.modules.user.dto.ChangePasswordRequest;
import com.ecommerce.backend.modules.user.dto.UpdateProfileRequest;
import com.ecommerce.backend.modules.user.dto.UserProfileResponse;
import com.ecommerce.backend.modules.user.entity.User;
import com.ecommerce.backend.modules.user.repository.UserRepository;
import com.ecommerce.backend.shared.exception.InvalidCredentialsException;
import com.ecommerce.backend.shared.exception.UserNotFoundException;
import com.ecommerce.backend.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPasswordHash("oldPasswordHash");
    }

    @Test
    void getCurrentUserProfile_whenUserExists_shouldReturnProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse profile = userService.getCurrentUserProfile(1L);

        assertThat(profile).isNotNull();
        assertThat(profile.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void getCurrentUserProfile_whenUserDoesNotExist_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUserProfile(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateProfile_whenUserExists_shouldUpdateAndReturnProfile() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        UserProfileResponse profile = userService.updateProfile(1L, request);

        assertThat(profile.getFirstName()).isEqualTo("Updated");
        assertThat(profile.getLastName()).isEqualTo("Name");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void changePassword_whenPasswordsMatchAndOldPasswordIsCorrect_shouldChangePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setNewPasswordConfirm("newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "oldPasswordHash")).thenReturn(true);
        when(passwordEncoder.matches("newPassword", "oldPasswordHash")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newPasswordHash");

        userService.changePassword(1L, request);

        verify(userRepository, times(1)).save(user);
        assertThat(user.getPasswordHash()).isEqualTo("newPasswordHash");
    }

    @Test
    void changePassword_whenNewPasswordsDoNotMatch_shouldThrowException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPassword");
        request.setNewPasswordConfirm("differentPassword");

        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Пароли не совпадают");
    }

    @Test
    void changePassword_whenOldPasswordIsIncorrect_shouldThrowException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongOldPassword");
        request.setNewPassword("newPassword");
        request.setNewPasswordConfirm("newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPassword", "oldPasswordHash")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Неверный текущий пароль");
    }

    @Test
    void changePassword_whenNewPasswordIsSameAsOld_shouldThrowException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setNewPasswordConfirm("newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "oldPasswordHash")).thenReturn(true);
        when(passwordEncoder.matches("newPassword", "oldPasswordHash")).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Новый пароль и текущий должны отличаться");
    }
}
