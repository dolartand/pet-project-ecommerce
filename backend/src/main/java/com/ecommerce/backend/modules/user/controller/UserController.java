package com.ecommerce.backend.modules.user.controller;

import com.ecommerce.backend.modules.user.dto.UpdateProfileRequest;
import com.ecommerce.backend.modules.user.dto.UserProfileResponse;
import com.ecommerce.backend.modules.user.sevice.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse>  getCurrentUserProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserProfileResponse profile = userService.getCurrentUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserProfileResponse profile = userService.updateProfile(userId, updateRequest);
        return ResponseEntity.ok(profile);
    }
}
