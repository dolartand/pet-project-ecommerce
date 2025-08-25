package com.ecommerce.backend.shared.events;

import com.ecommerce.backend.modules.cart.entity.Cart;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartAbandonedEvent extends BaseEvent {

    private Cart cart;

    public CartAbandonedEvent(Cart cart) {
        super(cart.getId().toString());
        this.cart = cart;
    }
}
