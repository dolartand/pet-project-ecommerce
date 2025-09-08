package com.ecommerce.backend.modules.product.service;

import com.ecommerce.backend.config.CacheConfig;
import com.ecommerce.backend.modules.category.repository.CategoryRepository;
import com.ecommerce.backend.modules.product.dto.CreateProductRequest;
import com.ecommerce.backend.modules.product.dto.ProductResponse;
import com.ecommerce.backend.modules.product.dto.ProductSearchRequest;
import com.ecommerce.backend.modules.product.dto.UpdateProductRequest;
import com.ecommerce.backend.modules.product.entity.Product;
import com.ecommerce.backend.modules.product.repository.ProductRepository;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import com.ecommerce.backend.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Cacheable(value = CacheConfig.CACHE_PRODUCTS, key = "#request.toString() + #pageable.toString()",
            condition = "#request.search == null && #request.categoryId == null && #pageable.getPageNumber() < 3")
    public Page<ProductResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
        log.info("Searching products with request: {} and pageable: {}", request, pageable);

        Pageable pageableWithSort =  PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );

        Page<Product> products = productRepository.findWithFilters(
                request.getSearch(),
                request.getCategoryId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getAvailable(),
                pageableWithSort
        );

        return products.map(this::mapToResponse);
    }

    @Cacheable(value = CacheConfig.CACHE_PRODUCT_DETAILS, key = "#id")
    public ProductResponse getProductById(Long id) {
        log.info("Getting product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with id: {}", id);
                    return ResourceNotFoundException.product(id);
                });
        log.info("Successfully fetched product with id: {}", id);
        return mapToResponse(product);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = CacheConfig.CACHE_PRODUCTS, allEntries = true)
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());

        validateCreateRequest(request);

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .categoryId(request.getCategoryId())
                .imageUrl(request.getImageUrl())
                .available(request.getAvailable())
                .rating(BigDecimal.ZERO)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created: {}", savedProduct.getId());

        return mapToResponse(savedProduct);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Caching(
            put = { @CachePut(value = CacheConfig.CACHE_PRODUCT_DETAILS, key = "#id") },
            evict = { @CacheEvict(value = CacheConfig.CACHE_PRODUCTS, allEntries = true) }
    )
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.product(id));

        updateProductFields(product, request);

        Product updatedProduct = productRepository.save(product);

        log.info("Product updated: {}", updatedProduct.getId());
        return mapToResponse(updatedProduct);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_PRODUCTS, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_PRODUCT_DETAILS, key = "#id")
    })
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);

        if (!productRepository.existsById(id)) {
            throw ResourceNotFoundException.product(id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted: {}", id);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_PRODUCT_DETAILS, key = "#id")
    public void updateProductRating(Long id, BigDecimal newRating) {
        log.info("Updating product rating with id: {}", id);
        productRepository.updateRating(id, newRating);
    }

    private void validateCreateRequest(CreateProductRequest request) {
        log.debug("Validating create product request for product: {}", request.getName());
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Validation failed for creating product {}: Price must be greater than zero.", request.getName());
            throw new ValidationException("Цена должна быть больше нуля");
        }

        if (!categoryRepository.existsById(request.getCategoryId())) {
            log.error("Validation failed for creating product {}: Category with id {} not found.", request.getName(), request.getCategoryId());
            throw ResourceNotFoundException.category(request.getCategoryId());
        }
    }

    private void updateProductFields(Product product, UpdateProductRequest request) {
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Цена должна быть больше нуля");
            }
            product.setPrice(request.getPrice());
        }
        if (request.getCategoryId() != null) {
            product.setCategoryId(request.getCategoryId());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getAvailable() != null) {
            product.setAvailable(request.getAvailable());
        }
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategoryId())
                .imageUrl(product.getImageUrl())
                .available(product.getAvailable())
                .rating(product.getRating())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
