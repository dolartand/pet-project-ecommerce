package com.ecommerce.backend.modules.inventory.service;

import com.ecommerce.backend.modules.order.entity.Order;
import com.ecommerce.backend.modules.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final InventoryService inventoryService;

    @RabbitListener(queues = "inventory.events")
    public void handleOrderEvent(Order order) {
        log.info("Received order event for inventory processing: order ID {}, status {}", order.getId(), order.getStatus());

        String userEmail = order.getUser().getEmail();

        if (order.getStatus() == OrderStatus.PENDING) {
            Map<Long, Integer> productQuantities = order.getItems().stream()
                    .collect(Collectors.toMap(item -> item.getId(), item -> item.getQuantity()));
            inventoryService.reserveProduct(order.getId(), productQuantities, userEmail);
        } else if (order.getStatus() == OrderStatus.CANCELLED) {
            inventoryService.cancelReservation(order.getId(), userEmail);
        } else if (order.getStatus() == OrderStatus.SHIPPED) {
            inventoryService.confirmReservation(order.getId(), userEmail);
        }
    }
}
