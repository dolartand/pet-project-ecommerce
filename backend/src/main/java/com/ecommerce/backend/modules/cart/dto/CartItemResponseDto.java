package com.ecommerce.backend.modules.cart.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CartItemResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal price;
}
