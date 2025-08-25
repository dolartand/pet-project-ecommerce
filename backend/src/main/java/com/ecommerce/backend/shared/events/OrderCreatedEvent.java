package com.ecommerce.backend.shared.events;

import com.ecommerce.backend.modules.order.entity.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreatedEvent extends  BaseEvent{

    private Order order;

    public OrderCreatedEvent(Order order) {
        super(order.getId().toString());
        this.order = order;
    }
}
