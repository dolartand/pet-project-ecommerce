package com.ecommerce.backend.modules.user.dto;

import com.ecommerce.backend.modules.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    List<AdminUserDto> users;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
