package com.ecommerce.backend.modules.order.controller;

import com.ecommerce.backend.modules.order.dto.OrderDto;
import com.ecommerce.backend.modules.order.dto.OrderStatusUpdateRequest;
import com.ecommerce.backend.modules.order.dto.OrdersPage;
import com.ecommerce.backend.modules.order.entity.OrderStatus;
import com.ecommerce.backend.modules.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<OrdersPage> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status
    ) {
        Pageable pageable = PageRequest.of(page, size);

        if (status != null) {
            return ResponseEntity.ok(orderService.getOrderByStatus(status, pageable));
        } else {
            return ResponseEntity.ok(orderService.getAllOrders(pageable));
        }
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request, principal.getName()));
    }
}
