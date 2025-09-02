package com.ecommerce.backend.shared.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Shipping street cannot be empty")
    private String shippingStreet;

    @NotNull(message = "Shipping city cannot be empty")
    private String shippingCity;

    @NotNull(message = "Shipping postal code cannot be empty")
    private String shippingPostalCode;
}
