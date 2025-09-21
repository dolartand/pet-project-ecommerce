package com.ecommerce.backend.modules.user.controller;

import com.ecommerce.backend.modules.user.dto.UpdateProfileRequest;
import com.ecommerce.backend.modules.user.dto.UserProfileResponse;
import com.ecommerce.backend.modules.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("Fetching profile for user with id: {}", userId);
        UserProfileResponse profile = userService.getCurrentUserProfile(userId);
        log.info("Successfully fetched profile for user with id: {}", userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("Updating profile for user with id: {}. Update: {}", userId, updateRequest);
        UserProfileResponse profile = userService.updateProfile(userId, updateRequest);
        log.info("Successfully updated profile for user with id: {}", userId);
        return ResponseEntity.ok(profile);
    }
}
