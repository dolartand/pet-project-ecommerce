package com.ecommerce.backend.modules.order.dto;

import com.ecommerce.backend.shared.dto.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Shipping address cannot be empty")
    @Valid
    private AddressDto address;

    @Size(max = 500, message = "Comment cannot be longer than 500 symbols")
    private String comment;
}
