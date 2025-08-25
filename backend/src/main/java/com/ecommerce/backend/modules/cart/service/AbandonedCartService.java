package com.ecommerce.backend.modules.cart.service;

import com.ecommerce.backend.config.RabbitConfig;
import com.ecommerce.backend.modules.cart.entity.Cart;
import com.ecommerce.backend.modules.cart.repository.CartRepository;
import com.ecommerce.backend.shared.events.CartAbandonedEvent;
import com.ecommerce.backend.shared.outbox.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbandonedCartService {

    private final CartRepository cartRepository;
    private final EventPublisher eventPublisher;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void findAndNotifyAbandonedCarts() {
        log.info("Running scheduled findAndNotifyAbandonedCarts...");
        LocalDateTime endTime = LocalDateTime.now().minusHours(24);
        LocalDateTime startTime = endTime.minusHours(1);

        List<Cart> abandonedCarts = cartRepository.findAbandonedCarts(startTime, endTime);

        if (abandonedCarts.isEmpty()) {
            log.info("No abandoned carts found in the last hour");
            return;
        }

        log.info("Found {} abandoned carts", abandonedCarts.size());
        for (Cart cart : abandonedCarts) {
            CartAbandonedEvent event = new  CartAbandonedEvent(cart);
            eventPublisher.publish(event, RabbitConfig.CART_EVENTS_EXCHANGE, "cart.abandoned");
            log.debug("Sending CART_ABANDONED event for cart with id: {}", cart.getId());
        }
        log.info("Finished scheduled findAndNotifyAbandonedCarts...");
    }
}
