package com.ecommerce.backend.modules.inventory.controller;

import com.ecommerce.backend.modules.inventory.dto.CartItemAvailabilityCheck;
import com.ecommerce.backend.modules.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
    
    @PostMapping("/check-availability")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestBody List<CartItemAvailabilityCheck> items
    ) {
        log.info("Request to check availability for items: {}", items);
        boolean available = inventoryService.checkAvailability(items);
        log.info("Availability check result: {}", available);
        return ResponseEntity.ok(available);
    }

    @GetMapping("product/{productId}/available")
    public ResponseEntity<Boolean> isProductAvailable(
            @PathVariable("productId") Long productId,
            @RequestParam Integer quantity
    ) {
        log.info("Request to check if product {} is available with quantity {}", productId, quantity);
        boolean available = inventoryService.isProductAvailable(productId, quantity);
        log.info("Product {} availability check result: {}", productId, available);
        return  ResponseEntity.ok(available);
    }
}
