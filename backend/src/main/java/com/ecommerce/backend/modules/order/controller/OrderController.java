package com.ecommerce.backend.modules.order.controller;

import com.ecommerce.backend.modules.order.dto.CreateOrderRequest;
import com.ecommerce.backend.modules.order.dto.OrderDto;
import com.ecommerce.backend.modules.order.dto.OrdersPage;
import com.ecommerce.backend.modules.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Principal principal
    ) {
        OrderDto order = orderService.createOrder(principal.getName(), request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<OrdersPage> getOrderHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal
    ) {
        Pageable pageable = PageRequest.of(page, size);
        OrdersPage orders = orderService.getUserOrders(principal.getName(), pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrderDetails(
            @PathVariable("orderId") Long orderId,
            Principal principal
    ) {
        OrderDto order = orderService.getOrderById(principal.getName(), orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable("orderId") Long orderId,
            Principal principal
    ) {
        OrderDto order = orderService.closeOrder(principal.getName(), orderId);
        return ResponseEntity.ok(order);
    }
}
