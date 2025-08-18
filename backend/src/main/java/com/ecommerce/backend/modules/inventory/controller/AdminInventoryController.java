package com.ecommerce.backend.modules.inventory.controller;

import com.ecommerce.backend.modules.inventory.dto.InventoryDto;
import com.ecommerce.backend.modules.inventory.dto.InventoryHistoryPage;
import com.ecommerce.backend.modules.inventory.dto.InventoryPage;
import com.ecommerce.backend.modules.inventory.dto.InventoryUpdateRequest;
import com.ecommerce.backend.modules.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<InventoryPage> getAllInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(inventoryService.getAllInventory(pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDto> getProductInventory(
            @PathVariable("productId") Long productId
    ) {
        return ResponseEntity.ok(inventoryService.getProductInventory(productId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<InventoryDto> updateInventory(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody InventoryUpdateRequest req,
            Principal principal
    ) {
        return ResponseEntity.ok(inventoryService.updateInventory(productId, req, principal.getName()));
    }

    @GetMapping("/{productId}/history")
    public ResponseEntity<InventoryHistoryPage> getInventoryHistory(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(inventoryService.getInventoryHistory(productId, pageable));
    }
}
