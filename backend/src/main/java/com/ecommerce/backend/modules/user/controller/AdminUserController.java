package com.ecommerce.backend.modules.user.controller;

import com.ecommerce.backend.modules.user.dto.AdminUserDto;
import com.ecommerce.backend.modules.user.dto.UserListResponse;
import com.ecommerce.backend.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserListResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String orderBy
    ) {
        log.info("Admin request to get all users. Page: {}, Size: {}, SortBy: {}, OrderBy: {}", page, size, sortBy, orderBy);
        Sort sort = Sort.by(Sort.Direction.fromString(orderBy), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminUserDto> users = userService.getAllUsers(pageable);

        UserListResponse userListResponse = UserListResponse.builder()
                .users(users.getContent())
                .totalPages(users.getTotalPages())
                .totalElements(users.getTotalElements())
                .currentPage(users.getNumber())
                .pageSize(users.getTotalPages())
                .build();

        log.info("Admin successfully fetched all users. Total elements: {}", users.getTotalElements());
        return ResponseEntity.ok(userListResponse);
    }
}
