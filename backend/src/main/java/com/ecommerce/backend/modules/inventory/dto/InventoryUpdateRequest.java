package com.ecommerce.backend.modules.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Quantity should not be empty")
    @Min(value = 0, message = "Quantity should not be negative")
    private Integer quantity;
}
