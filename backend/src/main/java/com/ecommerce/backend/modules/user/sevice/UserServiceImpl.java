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
        log.info("Fetching profile for user with id: {}", userId);
        User user = findUserById(userId);

        log.info("Successfully fetched profile for user with id: {}", userId);
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
        log.info("Updating profile for user with id: {}. Update: {}", userId, updateProfileRequest);
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
        log.info("Changing password for user with id: {}", userId);
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            log.error("Password change failed for user {}: Passwords do not match.", userId);
            throw new ValidationException("Пароли не совпадают");
        }

        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            log.error("Password change failed for user {}: Invalid current password.", userId);
            throw new InvalidCredentialsException("Неверный текущий пароль");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            log.error("Password change failed for user {}: New password is the same as the old one.", userId);
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
        log.info("Admin fetching all users. Pageable: {}", pageable);
        Page<User> users = userRepository.findAll(pageable);

        log.info("Admin successfully fetched {} users", users.getTotalElements());
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
        log.info("Admin fetching user by id: {}", userId);
        User user = findUserById(userId);

        int totalUserOrders = orderService.getUserOrders(user.getEmail(), Pageable.unpaged()).getContent().size();

        log.info("Admin successfully fetched user by id: {}", userId);
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
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new UserNotFoundException("Пользователь с ID: " + userId + " не найден");
                });
    }
}
