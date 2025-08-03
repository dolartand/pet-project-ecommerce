package com.ecommerce.backend.modules.category.service;

import com.ecommerce.backend.modules.category.dto.CategoryDto;
import com.ecommerce.backend.modules.category.entity.Category;
import com.ecommerce.backend.modules.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<CategoryDto> getAllRootCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("No such category with id: " + id));
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category  category = new Category();
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());

        if (categoryDto.getParentId() != null) {
            category.setParent(categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("No such category with id: " + categoryDto.getParentId())));
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Created category with id: {}", savedCategory.getId());
        return mapToDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No such category with id: " + id));
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        if (categoryDto.getParentId() != null) {
            category.setParent(categoryRepository.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("No such category with id: " + id)));
        } else category.setParent(null);
        Category updatedCategory = categoryRepository.save(category);
        log.info("Updated category with id: {}", updatedCategory.getId());
        return mapToDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) throw new EntityNotFoundException("No such category with id: " + id);
        categoryRepository.deleteById(id);
        log.info("Deleted category with id: {}", id);
    }

    private CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent().getId())
                .subcategories(category.getSubcategories())
                .build();
    }
}
