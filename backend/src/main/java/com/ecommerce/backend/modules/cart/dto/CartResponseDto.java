package com.ecommerce.backend.modules.cart.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<CartItemResponseDto> items;
    private BigDecimal totalAmount;
}
