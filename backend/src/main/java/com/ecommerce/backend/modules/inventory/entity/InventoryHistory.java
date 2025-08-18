package com.ecommerce.backend.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "change_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "available_before", nullable = false)
    private Integer availableBefore;

    @Column(name = "available_after", nullable = false)
    private Integer availableAfter;

    @Column(name = "reserved_before")
    private Integer reservedBefore;

    @Column(name = "reserved_after")
    private Integer reservedAfter;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ChangeType {
        ADD_STOCK,
        REMOVE_STOCK,
        RESERVE,
        RELEASE_RESERVE,
        CONFIRM_RESERVE
    }
}
