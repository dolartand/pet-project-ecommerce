package com.ecommerce.backend.modules.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UpdateProductRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(max=255)
    private String name;

    @Size(max=2000)
    private String description;

    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    private Long categoryId;

    private String imageUrl;

    private Boolean available;

    private BigDecimal rating;
}
