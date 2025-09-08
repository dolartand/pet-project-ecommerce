package com.ecommerce.backend.modules.order.controller;

import com.ecommerce.backend.modules.order.dto.CreateOrderRequest;
import com.ecommerce.backend.modules.order.dto.OrderDto;
import com.ecommerce.backend.modules.order.dto.OrdersPage;
import com.ecommerce.backend.modules.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Principal principal
    ) {
        log.info("Request from user {} to create order: {}", principal.getName(), request);
        OrderDto order = orderService.createOrder(principal.getName(), request);
        log.info("Successfully created order with id {} for user {}", order.getId(), principal.getName());
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<OrdersPage> getOrderHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal
    ) {
        log.info("Request from user {} to get order history. Page: {}, Size: {}", principal.getName(), page, size);
        Pageable pageable = PageRequest.of(page, size);
        OrdersPage orders = orderService.getUserOrders(principal.getName(), pageable);
        log.info("Successfully fetched order history for user {}. Total elements: {}", principal.getName(), orders.getPage().getTotalElements());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrderDetails(
            @PathVariable("orderId") Long orderId,
            Principal principal
    ) {
        log.info("Request from user {} to get details for order with id: {}", principal.getName(), orderId);
        OrderDto order = orderService.getOrderById(principal.getName(), orderId);
        log.info("Successfully fetched details for order with id: {}", orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable("orderId") Long orderId,
            Principal principal
    ) {
        log.info("Request from user {} to cancel order with id: {}", principal.getName(), orderId);
        OrderDto order = orderService.closeOrder(principal.getName(), orderId);
        log.info("Successfully cancelled order with id: {}", orderId);
        return ResponseEntity.ok(order);
    }
}
