package com.ecommerce.backend.modules.user.sevice;

import com.ecommerce.backend.modules.user.dto.AdminUserDto;
import com.ecommerce.backend.modules.user.dto.ChangePasswordRequest;
import com.ecommerce.backend.modules.user.dto.UpdateProfileRequest;
import com.ecommerce.backend.modules.user.dto.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserProfileResponse getCurrentUserProfile(Long userId);

    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest updateProfileRequest);

    void changePassword(Long userId, ChangePasswordRequest changePasswordRequest);

    Page<AdminUserDto> getAllUsers(Pageable pageable);

    AdminUserDto getUserById(Long userId);
}
