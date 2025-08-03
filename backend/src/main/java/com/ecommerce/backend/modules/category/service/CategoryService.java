package com.ecommerce.backend.modules.category.service;

import com.ecommerce.backend.modules.category.dto.CategoryDto;
import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAllRootCategories();

    CategoryDto getCategoryById(Long id);

    CategoryDto createCategory(CategoryDto categoryDto);

    CategoryDto updateCategory(Long id, CategoryDto categoryDto);

    void deleteCategory(Long id);
}
