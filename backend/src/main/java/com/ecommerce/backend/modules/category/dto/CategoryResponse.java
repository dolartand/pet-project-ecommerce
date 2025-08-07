package com.ecommerce.backend.modules.category.dto;

import com.ecommerce.backend.modules.category.entity.Category;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CategoryResponse> subcategories;
}
