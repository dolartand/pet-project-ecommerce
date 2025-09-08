package com.ecommerce.backend.modules.inventory.controller;

import com.ecommerce.backend.modules.inventory.dto.InventoryDto;
import com.ecommerce.backend.modules.inventory.dto.InventoryHistoryPage;
import com.ecommerce.backend.modules.inventory.dto.InventoryPage;
import com.ecommerce.backend.modules.inventory.dto.InventoryUpdateRequest;
import com.ecommerce.backend.modules.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<InventoryPage> getAllInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Admin request to get all inventory. Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        InventoryPage inventoryPage = inventoryService.getAllInventory(pageable);
        log.info("Admin successfully fetched all inventory. Total elements: {}", inventoryPage.getPage().getTotalElements());
        return ResponseEntity.ok(inventoryPage);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDto> getProductInventory(
            @PathVariable("productId") Long productId
    ) {
        log.info("Admin request to get inventory for product with id: {}", productId);
        InventoryDto inventoryDto = inventoryService.getProductInventory(productId);
        log.info("Admin successfully fetched inventory for product with id: {}. Inventory: {}", productId, inventoryDto);
        return ResponseEntity.ok(inventoryDto);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<InventoryDto> updateInventory(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody InventoryUpdateRequest req,
            Principal principal
    ) {
        log.info("Admin request to update inventory for product with id: {}. Update: {}", productId, req);
        InventoryDto updatedInventory = inventoryService.updateInventory(productId, req, principal.getName());
        log.info("Admin successfully updated inventory for product with id: {}. Result: {}", productId, updatedInventory);
        return ResponseEntity.ok(updatedInventory);
    }

    @GetMapping("/{productId}/history")
    public ResponseEntity<InventoryHistoryPage> getInventoryHistory(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Admin request to get inventory history for product with id: {}. Page: {}, Size: {}", productId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        InventoryHistoryPage historyPage = inventoryService.getInventoryHistory(productId, pageable);
        log.info("Admin successfully fetched inventory history for product with id: {}. Total elements: {}", productId, historyPage.getPage().getTotalElements());
        return ResponseEntity.ok(historyPage);
    }
}
