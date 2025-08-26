package com.ecommerce.backend.modules.category.service;

import com.ecommerce.backend.config.CacheConfig;
import com.ecommerce.backend.modules.category.dto.CategoryDto;
import com.ecommerce.backend.modules.category.dto.CategoryRequest;
import com.ecommerce.backend.modules.category.dto.CategoryResponse;
import com.ecommerce.backend.modules.category.entity.Category;
import com.ecommerce.backend.modules.category.repository.CategoryRepository;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Cacheable(CacheConfig.CACHE_CATEGORIES)
    public List<CategoryResponse> getAllRootCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "#id")
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> ResourceNotFoundException.category(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        Category  category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        if (request.getParentId() != null) {
            category.setParent(categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("No such category with id: " + request.getParentId())));
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Created category with id: {}", savedCategory.getId());
        return mapToResponse(savedCategory);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No such category with id: " + id));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getParentId() != null) {
            category.setParent(categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("No such category with id: " + id)));
        } else category.setParent(null);
        Category updatedCategory = categoryRepository.save(category);
        log.info("Updated category with id: {}", updatedCategory.getId());
        return mapToResponse(updatedCategory);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) throw new EntityNotFoundException("No such category with id: " + id);
        categoryRepository.deleteById(id);
        log.info("Deleted category with id: {}", id);
    }

    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse categoryResponse = new CategoryResponse();

        categoryResponse.setId(category.getId());
        categoryResponse.setName(category.getName());
        categoryResponse.setDescription(category.getDescription());

        if (category.getParent() != null) {
            categoryResponse.setParentId(category.getParent().getId());
        } else {
            categoryResponse.setParentId(null);
        }

        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            List<CategoryResponse> subcategoryResponses = category.getSubcategories().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            categoryResponse.setSubcategories(subcategoryResponses);
        }

        return categoryResponse;
    }

    private CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent().getId())
                .build();
    }
}
