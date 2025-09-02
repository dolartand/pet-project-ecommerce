package com.ecommerce.backend.modules.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal priceAtTime;
}
