package com.ecommerce.backend.modules.product.controller;

import com.ecommerce.backend.modules.product.dto.CreateProductRequest;
import com.ecommerce.backend.modules.product.dto.ProductResponse;
import com.ecommerce.backend.modules.product.dto.UpdateProductRequest;
import com.ecommerce.backend.modules.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("Admin request to create product: {}", request);
        ProductResponse productResponse = productService.createProduct(request);
        log.info("Admin successfully created product: {}", productResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateProductRequest request) {
        log.info("Admin request to update product with id: {}. Update: {}", id, request);
        ProductResponse productResponse = productService.updateProduct(id, request);
        log.info("Admin successfully updated product with id: {}. Result: {}", id, productResponse);
        return ResponseEntity.ok(productResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("Admin request to delete product with id: {}", id);
        productService.deleteProduct(id);
        log.info("Admin successfully deleted product with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
