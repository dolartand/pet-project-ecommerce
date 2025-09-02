package com.ecommerce.backend.modules.cart.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CartItemDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long cartId;
    private Long productId;
    private Integer quantity;
}
