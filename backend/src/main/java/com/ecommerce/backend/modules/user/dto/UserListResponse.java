package com.ecommerce.backend.modules.user.dto;

import com.ecommerce.backend.modules.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    List<AdminUserDto> users;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
