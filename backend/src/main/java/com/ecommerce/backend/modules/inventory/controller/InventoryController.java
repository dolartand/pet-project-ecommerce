package com.ecommerce.backend.modules.inventory.controller;

import com.ecommerce.backend.modules.inventory.dto.CartItemAvailabilityCheck;
import com.ecommerce.backend.modules.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    
    @PostMapping("/check-availability")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestBody List<CartItemAvailabilityCheck> items
    ) {
        return ResponseEntity.ok(inventoryService.checkAvailability(items));
    }

    @GetMapping("product/{productId}/available")
    public ResponseEntity<Boolean> isProductAvailable(
            @PathVariable("productId") Long productId,
            @RequestParam Integer quantity
    ) {
        return  ResponseEntity.ok(inventoryService.isProductAvailable(productId, quantity));
    }
}
