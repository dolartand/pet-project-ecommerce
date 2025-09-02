package com.ecommerce.backend.modules.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemAvailabilityCheck implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private Integer quantity;
}
