package com.ecommerce.backend.modules.order.dto;

import com.ecommerce.backend.modules.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrderStatusUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Status cannot be empty")
    private OrderStatus status;
}
