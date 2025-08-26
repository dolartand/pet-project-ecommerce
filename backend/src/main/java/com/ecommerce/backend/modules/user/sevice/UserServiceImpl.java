package com.ecommerce.backend.modules.user.sevice;

import com.ecommerce.backend.config.CacheConfig;
import com.ecommerce.backend.modules.order.service.OrderService;
import com.ecommerce.backend.modules.user.dto.AdminUserDto;
import com.ecommerce.backend.modules.user.dto.ChangePasswordRequest;
import com.ecommerce.backend.modules.user.dto.UpdateProfileRequest;
import com.ecommerce.backend.modules.user.dto.UserProfileResponse;
import com.ecommerce.backend.modules.user.entity.User;
import com.ecommerce.backend.modules.user.repository.UserRepository;
import com.ecommerce.backend.shared.exception.InvalidCredentialsException;
import com.ecommerce.backend.shared.exception.UserNotFoundException;
import com.ecommerce.backend.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderService orderService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_USER_PROFILE, key = "#userId")
    public UserProfileResponse getCurrentUserProfile(Long userId) {
        User user = findUserById(userId);

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    @CachePut(value = CacheConfig.CACHE_USER_PROFILE, key = "#userId")
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest updateProfileRequest) {
        User user = findUserById(userId);

        user.setFirstName(updateProfileRequest.getFirstName());
        user.setLastName(updateProfileRequest.getLastName());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);

        log.info("Profile updated for user: {}", updatedUser.getEmail());

        return UserProfileResponse.builder()
                .id(updatedUser.getId())
                .email(updatedUser.getEmail())
                .firstName(updatedUser.getFirstName())
                .lastName(updatedUser.getLastName())
                .role(updatedUser.getRole())
                .createdAt(updatedUser.getCreatedAt())
                .updatedAt(updatedUser.getUpdatedAt())
                .build();
    }

    /**
     * На данный момент метод не используется, так как реализуется базовый функционал
     */
    @Override
    @CacheEvict(value = CacheConfig.CACHE_USER_PROFILE, key = "#userId")
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new ValidationException("Пароли не совпадают");
        }

        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Неверный текущий пароль");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ValidationException("Новый пароль и текущий должны отличаться");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminUserDto> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);

        return users.map(user -> AdminUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .totalOrders(orderService.getUserOrders(user.getEmail(), pageable).getContent().size())
                .build()
        );
    }

    /**
     * На данный момент метод не используется, так как реализуется базовый функционал
     */
    @Override
    @Transactional(readOnly = true)
    public AdminUserDto getUserById(Long userId) {
        User user = findUserById(userId);

        int totalUserOrders = orderService.getUserOrders(user.getEmail(), Pageable.unpaged()).getContent().size();

        return AdminUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .totalOrders(totalUserOrders)
                .build();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID: " + userId + " не найден"));
    }
}
