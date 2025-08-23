package com.ecommerce.backend.modules.category.service;

import com.ecommerce.backend.modules.category.dto.CategoryRequest;
import com.ecommerce.backend.modules.category.dto.CategoryResponse;
import com.ecommerce.backend.modules.category.entity.Category;
import com.ecommerce.backend.modules.category.repository.CategoryRepository;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryServiceImpl;

    private Category category;
    private Category parentCategory;

    @BeforeEach
    public void setUp() {
        parentCategory = new Category();
        parentCategory.setId(1L);
        parentCategory.setName("Parent");

        category = new Category();
        category.setId(2L);
        category.setName("Child");
        category.setParent(parentCategory);
    }

    @Test
    void getAllRootCategories_shouldReturnRootCategories() {
        when(categoryRepository.findByParentIsNull()).thenReturn(Collections.singletonList(parentCategory));

        List<CategoryResponse> responses = categoryServiceImpl.getAllRootCategories();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Parent");
    }

    @Test
    void getCategoryById_whenCategoryExists_shouldReturnCategory() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        CategoryResponse categoryResponse = categoryServiceImpl.getCategoryById(2L);

        assertThat(categoryResponse.getName()).isEqualTo("Child");
        assertThat(categoryResponse.getParentId()).isEqualTo(1L);
    }

    @Test
    void getCategoryById_whenCategoryDoesNotExist_shouldThrowException() {
        when(categoryRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryServiceImpl.getCategoryById(3L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createCategory_withParent_shouldCreateCategory() {
        CategoryRequest request = new CategoryRequest();
        request.setName("New Category");
        request.setParentId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> {
            Category savedCategory = i.getArgument(0);
            savedCategory.setId(3L);
            return savedCategory;
        });

        CategoryResponse categoryResponse = categoryServiceImpl.createCategory(request);

        assertThat(categoryResponse.getName()).isEqualTo("New Category");
        assertThat(categoryResponse.getParentId()).isEqualTo(1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_withoutParent_shouldCreateRootCategory() {
        CategoryRequest request = new CategoryRequest();
        request.setName("New Root Category");

        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> {
            Category savedCategory = i.getArgument(0);
            savedCategory.setId(3L);
            return savedCategory;
        });

        CategoryResponse categoryResponse = categoryServiceImpl.createCategory(request);

        assertThat(categoryResponse.getName()).isEqualTo("New Root Category");
        assertThat(categoryResponse.getParentId()).isNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_shouldUpdateCategory() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Category");

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        CategoryResponse response = categoryServiceImpl.updateCategory(2L, request);

        assertThat(response.getName()).isEqualTo("Updated Category");
        verify(categoryRepository).save(category);
    }

    @Test
    void deleteCategory_shouldDeleteCategory() {
        when(categoryRepository.existsById(2L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(2L);

        categoryServiceImpl.deleteCategory(2L);

        verify(categoryRepository).deleteById(2L);
    }

    @Test
    void deleteCategory_whenCategoryDoesNotExist_shouldThrowException() {
        when(categoryRepository.existsById(3L)).thenReturn(false);

        assertThatThrownBy(() -> categoryServiceImpl.deleteCategory(3L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
