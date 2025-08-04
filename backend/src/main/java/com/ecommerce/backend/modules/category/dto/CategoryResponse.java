package com.ecommerce.backend.modules.category.dto;

import com.ecommerce.backend.modules.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private String name;
    private String description;
    private Long parentId;
    private List<Category> subcategories;
}
