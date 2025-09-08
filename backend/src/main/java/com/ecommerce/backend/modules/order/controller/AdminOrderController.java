package com.ecommerce.backend.modules.order.controller;

import com.ecommerce.backend.modules.order.dto.OrderDto;
import com.ecommerce.backend.modules.order.dto.OrderStatusUpdateRequest;
import com.ecommerce.backend.modules.order.dto.OrdersPage;
import com.ecommerce.backend.modules.order.entity.OrderStatus;
import com.ecommerce.backend.modules.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<OrdersPage> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status
    ) {
        log.info("Admin request to get all orders. Page: {}. Size: {}. Status: {}.", page, size, status);
        Pageable pageable = PageRequest.of(page, size);

        OrdersPage ordersPage;
        if (status != null) {
            ordersPage = orderService.getOrderByStatus(status, pageable);
            log.info("Admin successfully fetched orders with status {}. Total elements: {}", status, ordersPage.getPage().getTotalElements());
        } else {
            ordersPage = orderService.getAllOrders(pageable);
            log.info("Admin successfully fetched all orders. Total elements: {}", ordersPage.getPage().getTotalElements());
        }
        return ResponseEntity.ok(ordersPage);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            Principal principal
    ) {
        log.info("Admin request to update status for order with id: {}. Update: {}", orderId, request);
        OrderDto updatedOrder = orderService.updateOrderStatus(orderId, request, principal.getName());
        log.info("Admin successfully updated status for order with id: {}. Result: {}", orderId, updatedOrder);
        return ResponseEntity.ok(updatedOrder);
    }
}
