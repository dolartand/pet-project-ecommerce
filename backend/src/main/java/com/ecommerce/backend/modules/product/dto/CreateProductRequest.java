package com.ecommerce.backend.modules.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CreateProductRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max=255)
    private String name;

    @Size(max=2000)
    private String description;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @NotNull
    private Long categoryId;

    @NotNull
    private String imageUrl;

    @NotNull
    private Boolean available;
}
