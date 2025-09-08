package com.ecommerce.backend.modules.product.controller;

import com.ecommerce.backend.modules.product.dto.ProductResponse;
import com.ecommerce.backend.modules.product.dto.ProductSearchRequest;
import com.ecommerce.backend.modules.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @Valid @ModelAttribute ProductSearchRequest request,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Request to search products with request: {} and pageable: {}", request, pageable);
        Page<ProductResponse> products = productService.searchProducts(request, pageable);
        log.info("Successfully searched products. Found {} products.", products.getTotalElements());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        log.info("Request to get product by id: {}", id);
        ProductResponse product = productService.getProductById(id);
        log.info("Successfully fetched product by id: {}. Result: {}", id, product);
        return ResponseEntity.ok(product);
    }
}
