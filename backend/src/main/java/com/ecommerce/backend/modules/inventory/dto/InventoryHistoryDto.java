package com.ecommerce.backend.modules.inventory.dto;

import com.ecommerce.backend.modules.inventory.entity.InventoryHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHistoryDto {
    private Long id;
    private Long productId;
    private String productName;
    private InventoryHistory.ChangeType changeType;
    private Integer quantity;
    private Integer availableBefore;
    private Integer availableAfter;
    private Integer reservedBefore;
    private Integer reservedAfter;
    private Long orderId;
    private String createdBy;
    private LocalDateTime createdAt;
}
