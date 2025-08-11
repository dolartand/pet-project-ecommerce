package com.ecommerce.backend.modules.cart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponseDto {
    private List<CartItemResponseDto> items;
    private BigDecimal totalAmount;
}
