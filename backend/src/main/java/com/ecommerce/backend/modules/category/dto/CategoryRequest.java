package com.ecommerce.backend.modules.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    private String description;

    private Long parentId;
}
