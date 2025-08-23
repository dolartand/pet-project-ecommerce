package com.ecommerce.backend.modules.product.service;

import com.ecommerce.backend.modules.category.repository.CategoryRepository;
import com.ecommerce.backend.modules.product.dto.CreateProductRequest;
import com.ecommerce.backend.modules.product.dto.ProductResponse;
import com.ecommerce.backend.modules.product.dto.UpdateProductRequest;
import com.ecommerce.backend.modules.product.entity.Product;
import com.ecommerce.backend.modules.product.repository.ProductRepository;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import com.ecommerce.backend.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private CreateProductRequest createRequest;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(BigDecimal.TEN);
        product.setCategoryId(1L);
        product.setAvailable(true);

        createRequest = new CreateProductRequest();
        createRequest.setName("New Product");
        createRequest.setPrice(BigDecimal.valueOf(20));
        createRequest.setCategoryId(1L);
        createRequest.setAvailable(true);
    }

    @Test
    void getProductById_whenProductExists_shouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Product");
    }

    @Test
    void getProductById_whenProductDoesNotExist_shouldThrowException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createProduct_whenRequestIsValid_shouldCreateProduct() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        ProductResponse response = productService.createProduct(createRequest);

        assertThat(response.getName()).isEqualTo("New Product");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_withInvalidPrice_shouldThrowException() {
        createRequest.setPrice(BigDecimal.ZERO);

        assertThatThrownBy(() -> productService.createProduct(createRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Цена должна быть больше нуля");
    }

    @Test
    void createProduct_withNonExistentCategory_shouldThrowException() {
        when(categoryRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> productService.createProduct(createRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProduct_whenProductExists_shouldUpdateProduct() {
        UpdateProductRequest updateRequest = new UpdateProductRequest();
        updateRequest.setName("Updated Product Name");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertThat(response.getName()).isEqualTo("Updated Product Name");
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_whenProductExists_shouldDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_whenProductDoesNotExist_shouldThrowException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProductRating_shouldCallRepositoryUpdate() {
        doNothing().when(productRepository).updateRating(anyLong(), any(BigDecimal.class));

        productService.updateProductRating(1L, BigDecimal.valueOf(4.5));

        verify(productRepository).updateRating(1L, BigDecimal.valueOf(4.5));
    }
}
