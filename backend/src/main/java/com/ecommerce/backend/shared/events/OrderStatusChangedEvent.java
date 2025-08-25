package com.ecommerce.backend.shared.events;

import com.ecommerce.backend.modules.order.entity.Order;
import com.ecommerce.backend.modules.order.entity.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderStatusChangedEvent extends BaseEvent {

    private Order order;
    private OrderStatus oldStatus;

    public OrderStatusChangedEvent(Order order, OrderStatus olStatus) {
        super(order.getId().toString());
        this.order = order;
        this.oldStatus = olStatus;
    }
}
