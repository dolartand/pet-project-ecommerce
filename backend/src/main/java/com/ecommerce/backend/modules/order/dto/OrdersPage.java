package com.ecommerce.backend.modules.order.dto;

import com.ecommerce.backend.shared.dto.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersPage implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<OrderDto> content;
    private PageInfo page;
}
