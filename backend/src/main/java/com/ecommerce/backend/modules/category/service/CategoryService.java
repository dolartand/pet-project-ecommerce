package com.ecommerce.backend.modules.category.service;

import com.ecommerce.backend.modules.category.dto.CategoryDto;
import com.ecommerce.backend.modules.category.dto.CategoryRequest;
import com.ecommerce.backend.modules.category.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllRootCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);
}
