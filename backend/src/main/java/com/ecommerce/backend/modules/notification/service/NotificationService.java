package com.ecommerce.backend.modules.notification.service;

import com.ecommerce.backend.modules.cart.entity.Cart;
import com.ecommerce.backend.modules.order.entity.Order;
import com.ecommerce.backend.modules.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @RabbitListener(queues = "email.notifications")
    public void handleUserRgistration(User user) {
        logger.info("Received email registration event for user: " + user.getEmail());
        // TODO: Реализовать отправку подтверждения по почте
    }

    @RabbitListener(queues = "order.notifications")
    public void handleOrderEvent(Order order) {
        logger.info("Received order event for notification: Order ID {}, Status {}", order.getId(), order.getStatus());
        // TODO: Реализовать отправку подтверждения по почте учитывая статус заказа
    }

    @RabbitListener(queues = "cart.abandoned.notifications")
    public void handleAbandonedCartEvent(Cart cart) {
        logger.info("Received abandoned cart event for notification: Cart ID {}", cart.getId());
        // TODO: Реализовать отправку подтверждения по почте для забытых корзин
    }
}
