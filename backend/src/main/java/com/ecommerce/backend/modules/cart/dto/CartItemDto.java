package com.ecommerce.backend.modules.cart.dto;

import lombok.Data;

@Data
public class CartItemDto {
    private Long cartId;
    private Long productId;
    private Integer quantity;
}
