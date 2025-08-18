package com.ecommerce.backend.modules.inventory.service;

import com.ecommerce.backend.modules.inventory.dto.*;
import com.ecommerce.backend.modules.inventory.entity.Inventory;
import com.ecommerce.backend.modules.inventory.entity.InventoryHistory;
import com.ecommerce.backend.modules.inventory.repository.InventoryHistoryRepository;
import com.ecommerce.backend.modules.inventory.repository.InventoryRepository;
import com.ecommerce.backend.modules.product.entity.Product;
import com.ecommerce.backend.modules.product.repository.ProductRepository;
import com.ecommerce.backend.shared.dto.PageInfo;
import com.ecommerce.backend.shared.exception.BusinessException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public InventoryDto getProductInventory(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("No data for product with id: " + productId,
                        "INVENTORY_NOT_FOUND"));
        return mapToDto(inventory, product.getName());
    }

    @Transactional(readOnly = true)
    public InventoryPage getAllInventory(Pageable pageable) {
        Page<Inventory> inventoryPage = inventoryRepository.findAll(pageable);

        List<Long> productIds = inventoryPage.getContent().stream()
                .map(Inventory::getProductId)
                .collect(Collectors.toList());

        Map<Long, String> productNames = new HashMap<>();
        if (!productIds.isEmpty()) {
            productRepository.findAllById(productIds).forEach(product ->
                    productNames.put(product.getId(), product.getName()));
        }

        List<InventoryDto> inventoryDtos = inventoryPage.getContent().stream()
                .map(inventory -> mapToDto(
                        inventory,
                        productNames.getOrDefault(inventory.getProductId(), "Unknown product")
                ))
                .collect(Collectors.toList());

        PageInfo page = PageInfo.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(inventoryPage.getTotalElements())
                .totalPages(inventoryPage.getTotalPages())
                .build();

        return InventoryPage.builder()
                .content(inventoryDtos)
                .page(page)
                .build();
    }

    @Transactional
    public InventoryDto updateInventory(Long productId, InventoryUpdateRequest request, String username) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseGet(() -> Inventory.builder()
                        .productId(productId)
                        .availableQuantity(0)
                        .reservedQuantity(0)
                        .build());

        int currentAvailable = inventory.getAvailableQuantity();
        inventory.setAvailableQuantity(request.getQuantity());

        Inventory savedInventory = inventoryRepository.save(inventory);

        createHistoryRecord(
                productId,
                request.getQuantity() > currentAvailable
                        ? InventoryHistory.ChangeType.ADD_STOCK
                        : InventoryHistory.ChangeType.REMOVE_STOCK,
                Math.abs(request.getQuantity() - currentAvailable),
                currentAvailable,
                request.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getReservedQuantity(),
                null,
                username
        );

        return mapToDto(savedInventory, product.getName());
    }

    public InventoryHistoryPage getInventoryHistory(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw ResourceNotFoundException.product(productId);
        }

        Page<InventoryHistory> historyPage = inventoryHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        String productName = productRepository.findById(productId)
                .map(Product::getName)
                .orElse("Unknown product");

        List<InventoryHistoryDto> historyDtos = historyPage.getContent().stream()
                .map(history -> mapToHistoryDto(history, productName))
                .collect(Collectors.toList());

        PageInfo pageInfo = PageInfo.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(historyPage.getTotalElements())
                .totalPages(historyPage.getTotalPages())
                .build();

        return InventoryHistoryPage.builder()
                .content(historyDtos)
                .page(pageInfo)
                .build();

    }

    @Transactional(readOnly = true)
    public boolean checkAvailability(List<CartItemAvailabilityCheck> items) {
        List<Long> productIds = items.stream()
                .map(CartItemAvailabilityCheck::getProductId)
                .collect(Collectors.toList());

        List<Inventory> inventories = inventoryRepository.findAllByProductIds(productIds);

        Map<Long, Inventory> mapInventories = inventories.stream()
                .collect(Collectors.toMap(Inventory::getProductId, inventory -> inventory));

        for (CartItemAvailabilityCheck item : items) {
            Inventory inventory = mapInventories.get(item.getProductId());
            if (inventory == null || inventory.getAvailableQuantity() < item.getQuantity()) {
                return false;
            }
        }

        return true;
    }

    @Transactional(readOnly = true)
    public boolean isProductAvailable(Long productId, Integer quantity) {
        return inventoryRepository.findById(productId)
                .map(inventory -> inventory.getAvailableQuantity() >= quantity)
                .orElse(false);
    }

    @Transactional
    public void reserveProduct(Long orderId, Map<Long, Integer> productQuantities, String username) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new BusinessException(
                            "No data for product  " + productId,
                            "INVENTORY_NOT_FOUND"));

            if (inventory.getAvailableQuantity() < quantity) {
                throw new BusinessException(
                        "Недостаточно товара на складе. Доступно: " + inventory.getAvailableQuantity() +
                                ", требуется: " + quantity,
                        "INSUFFICIENT_STOCK"
                );
            }

            int availableBefore = inventory.getAvailableQuantity();
            int reservedBefore = inventory.getReservedQuantity();

            inventory.setAvailableQuantity(availableBefore - quantity);
            inventory.setReservedQuantity(reservedBefore + quantity);

            inventoryRepository.save(inventory);

            createHistoryRecord(
                    productId,
                    InventoryHistory.ChangeType.RESERVE,
                    quantity,
                    availableBefore,
                    inventory.getAvailableQuantity(),
                    reservedBefore,
                    inventory.getReservedQuantity(),
                    orderId,
                    username
            );
        }
    }

    @Transactional
    public void confirmReservation(Long orderId, String username) {
        List<InventoryHistory> historyRecords = inventoryHistoryRepository.findByOrderId(orderId);

        Map<Long, Integer> productQuantities = new HashMap<>();

        for (InventoryHistory history : historyRecords) {
            if (history.getChangeType() == InventoryHistory.ChangeType.RESERVE) {
                productQuantities.put(
                        history.getProductId(),
                        history.getQuantity()
                );
            }
        }

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new BusinessException(
                            "No data for product  " + productId,
                            "INVENTORY_NOT_FOUND")
                    );

            int reservedBefore = inventory.getReservedQuantity();

            inventory.setReservedQuantity(reservedBefore - quantity);
            inventoryRepository.save(inventory);

            createHistoryRecord(
                    productId,
                    InventoryHistory.ChangeType.CONFIRM_RESERVE,
                    quantity,
                    inventory.getAvailableQuantity(),
                    inventory.getAvailableQuantity(),
                    reservedBefore,
                    inventory.getReservedQuantity(),
                    orderId,
                    username
            );
        }
    }

    @Transactional
    public void cancelReservation(Long orderId, String username) {
        List<InventoryHistory> historyRecords = inventoryHistoryRepository.findByOrderId(orderId);

        Map<Long, Integer> productQuantities = new HashMap<>();

        for (InventoryHistory history : historyRecords) {
            if (history.getChangeType() == InventoryHistory.ChangeType.RESERVE) {
                productQuantities.put(
                        history.getProductId(),
                        history.getQuantity()
                );
            }
        }

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new BusinessException(
                            "No data for product  " + productId,
                            "INVENTORY_NOT_FOUND"
                    ));

            int availableBefore = inventory.getAvailableQuantity();
            int reservedBefore = inventory.getReservedQuantity();

            inventory.setAvailableQuantity(availableBefore + quantity);
            inventory.setReservedQuantity(reservedBefore - quantity);

            inventoryRepository.save(inventory);

            createHistoryRecord(
                    productId,
                    InventoryHistory.ChangeType.RELEASE_RESERVE,
                    quantity,
                    availableBefore,
                    inventory.getAvailableQuantity(),
                    reservedBefore,
                    inventory.getReservedQuantity(),
                    orderId,
                    username
            );
        }
    }

    @Transactional
    public Inventory createInventoryForProduct(Product product, Integer initQuantity ,String username) {
        if (inventoryRepository.existsByProductId(product.getId())) {
            log.warn("Inventory already exists for product {}", product.getId());
            return inventoryRepository.findById(product.getId()).orElse(null);
        }

        Inventory inventory = Inventory.builder()
                .productId(product.getId())
                .availableQuantity(initQuantity)
                .reservedQuantity(0)
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);

        if (initQuantity > 0) {
            createHistoryRecord(
                    product.getId(),
                    InventoryHistory.ChangeType.ADD_STOCK,
                    initQuantity,
                    0,
                    initQuantity,
                    0,
                    0,
                    null,
                    username
            );
        }

        return savedInventory;
    }

    @Transactional
    public Inventory updateProductAvailability(Long productId, boolean available) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(
                        "No data for product  " + productId,
                        "INVENTORY_NOT_FOUND"
                ));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));

        product.setAvailable(available);
        productRepository.save(product);

        return inventory;
    }

    private  InventoryDto mapToDto(Inventory inventory, String productName) {
        return InventoryDto.builder()
                .productId(inventory.getProductId())
                .productName(productName)
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .totalQuantity(inventory.getReservedQuantity() +  inventory.getAvailableQuantity())
                .build();
    }

    private InventoryHistoryDto mapToHistoryDto(InventoryHistory history, String productName) {
        return InventoryHistoryDto.builder()
                .id(history.getId())
                .productId(history.getProductId())
                .productName(productName)
                .changeType(history.getChangeType())
                .quantity(history.getQuantity())
                .availableBefore(history.getAvailableBefore())
                .availableAfter(history.getAvailableAfter())
                .reservedBefore(history.getReservedBefore())
                .reservedAfter(history.getReservedAfter())
                .orderId(history.getOrderId())
                .createdBy(history.getCreatedBy())
                .createdAt(history.getCreatedAt())
                .build();
    }

    private void createHistoryRecord(
            Long productId,
            InventoryHistory.ChangeType changeType,
            Integer quantity,
            Integer availableBefore,
            Integer availableAfter,
            Integer reservedBefore,
            Integer reservedAfter,
            Long orderId,
            String username
    ) {
        InventoryHistory inventoryHistory = InventoryHistory.builder()
                .productId(productId)
                .changeType(changeType)
                .quantity(quantity)
                .availableBefore(availableBefore)
                .availableAfter(availableAfter)
                .reservedBefore(reservedBefore)
                .reservedAfter(reservedAfter)
                .orderId(orderId)
                .createdBy(username)
                .build();

        inventoryHistoryRepository.save(inventoryHistory);
        log.info("Inventory History Record created with id: {}", inventoryHistory.getId());
    }
}
