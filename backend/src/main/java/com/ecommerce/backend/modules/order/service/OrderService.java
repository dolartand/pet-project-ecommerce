package com.ecommerce.backend.modules.order.service;

import com.ecommerce.backend.config.RabbitConfig;
import com.ecommerce.backend.modules.cart.entity.Cart;
import com.ecommerce.backend.modules.cart.repository.CartRepository;
import com.ecommerce.backend.modules.cart.service.CartService;
import com.ecommerce.backend.modules.inventory.dto.InventoryUpdateRequest;
import com.ecommerce.backend.modules.inventory.service.InventoryService;
import com.ecommerce.backend.modules.order.dto.*;
import com.ecommerce.backend.modules.order.entity.Order;
import com.ecommerce.backend.modules.order.entity.OrderItem;
import com.ecommerce.backend.modules.order.entity.OrderStatus;
import com.ecommerce.backend.modules.order.repository.OrdersRepository;
import com.ecommerce.backend.modules.product.entity.Product;
import com.ecommerce.backend.modules.product.repository.ProductRepository;
import com.ecommerce.backend.modules.user.entity.User;
import com.ecommerce.backend.modules.user.repository.UserRepository;
import com.ecommerce.backend.shared.dto.AddressDto;
import com.ecommerce.backend.shared.dto.PageInfo;
import com.ecommerce.backend.shared.events.OrderCreatedEvent;
import com.ecommerce.backend.shared.events.OrderStatusChangedEvent;
import com.ecommerce.backend.shared.exception.BusinessException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import com.ecommerce.backend.shared.outbox.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public OrderDto createOrder(String userEmail, CreateOrderRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Cart cart = cartRepository.findByUserIdWithItems(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + user.getId()));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cannot create order with empty cart", "CREATE_CART_ERROR");
        }

        cart.getItems().forEach(cartItem -> {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + cartItem.getProduct().getId())
                    );
            if (!product.getAvailable()) {
                throw new BusinessException("Product unavailable " + product.getName(), "PRODUCT_UNAVAILABLE");
            }
        });

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(calculateTotal(cart))
                .shippingStreet(request.getAddress().getShippingStreet())
                .shippingCity(request.getAddress().getShippingCity())
                .shippingPostalCode(request.getAddress().getShippingPostalCode())
                .comment(request.getComment())
                .build();

        Map<Long, Integer> productQuantities = cart.getItems().stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item.getQuantity()));

        cart.getItems().forEach(cartItem -> {
            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProduct().getId())
                    .productName(cartItem.getProduct().getName())
                    .quantity(cartItem.getQuantity())
                    .priceAtTime(cartItem.getProduct().getPrice())
                    .build();
            order.addItem(orderItem);
        });

        Order savedOrder = ordersRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(savedOrder);
        eventPublisher.publish(event, RabbitConfig.ORDER_EVENTS_EXCHANGE, "order.created");

        cartService.clearCart(user.getId());

        return mapOrderToDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrdersPage getUserOrders(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        Page<Order> ordersPage = ordersRepository.findByUser(user, pageable);

        return OrdersPage.builder()
                .content(ordersPage.getContent().stream()
                        .map(this::mapOrderToDto)
                        .collect(Collectors.toList())
                )
                .page(PageInfo.builder()
                        .page(pageable.getPageNumber())
                        .size(pageable.getPageSize())
                        .totalElements(ordersPage.getTotalElements())
                        .totalPages(ordersPage.getTotalPages())
                        .build()
                )
                .build();
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(String userEmail, Long orderId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Order order = ordersRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        return mapOrderToDto(order);
    }

    @Transactional
    public OrderDto closeOrder(String userEmail, Long orderId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Order order = ordersRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.COMFIRMED) {
            throw new BusinessException("Order cannot be closed", "ORDER_CLOSING_ERROR");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = ordersRepository.save(order);

        OrderStatusChangedEvent event = new OrderStatusChangedEvent(savedOrder, oldStatus);
        eventPublisher.publish(event, RabbitConfig.ORDER_EVENTS_EXCHANGE, "order.status.changed");

        return mapOrderToDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrdersPage getAllOrders(Pageable pageable) {
        Page<Order> ordersPage = ordersRepository.findAll(pageable);

        return OrdersPage.builder()
                .content(ordersPage.getContent().stream()
                        .map(this::mapOrderToDto)
                        .collect(Collectors.toList())
                )
                .page(PageInfo.builder()
                        .page(pageable.getPageNumber())
                        .size(pageable.getPageSize())
                        .totalElements(ordersPage.getTotalElements())
                        .totalPages(ordersPage.getTotalPages())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public OrdersPage getOrderByStatus(OrderStatus orderStatus, Pageable pageable) {
        Page<Order> ordersPage = ordersRepository.findByStatus(orderStatus, pageable);

        return OrdersPage.builder()
                .content(ordersPage.getContent().stream()
                        .map(this::mapOrderToDto)
                        .collect(Collectors.toList()))
                .page(PageInfo.builder()
                        .page(pageable.getPageNumber())
                        .size(pageable.getPageSize())
                        .totalElements(ordersPage.getTotalElements())
                        .totalPages(ordersPage.getTotalPages())
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByIdAdmin(Long orderId) {
        Order order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapOrderToDto(order);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatusUpdateRequest request, String adminEmail) {
        Order order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus newStatus = request.getStatus();
        OrderStatus oldStatus = order.getStatus();

        validateStatusTransition(oldStatus, newStatus);

        order.setStatus(newStatus);
        Order savedOrder = ordersRepository.save(order);

        OrderStatusChangedEvent event = new OrderStatusChangedEvent(savedOrder, oldStatus);
        eventPublisher.publish(event, RabbitConfig.ORDER_EVENTS_EXCHANGE, "order.status.changed");

        return mapOrderToDto(savedOrder);
    }

    private void validateStatusTransition(OrderStatus oldStatus, OrderStatus newStatus) {
        if (oldStatus == OrderStatus.CANCELLED) {
            throw new BusinessException("Cancelled order cannot be closed", "ORDER_CLOSING_ERROR");
        }

        if (oldStatus == OrderStatus.DELIVERED && newStatus == OrderStatus.DELIVERED) {
            throw new BusinessException("Delivered order cannot be closed", "ORDER_CLOSING_ERROR");
        }

        if (oldStatus == OrderStatus.PENDING &&
                (newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.DELIVERED)) {
            throw new BusinessException("Order must be approved before shipping", "ORDER_CLOSING_ERROR");
        }

        if (oldStatus == OrderStatus.COMFIRMED && newStatus == OrderStatus.DELIVERED) {
            throw new BusinessException("Order must be shipped before delivering", "ORDER_CLOSING_ERROR");
        }
    }

    private BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProduct().getId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Item not found with id: " + item.getProduct().getId()));
                    return product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderDto mapOrderToDto(Order order) {
        User user = order.getUser();

        AddressDto addressDto = AddressDto.builder()
                .shippingStreet(order.getShippingStreet())
                .shippingCity(order.getShippingCity())
                .shippingPostalCode(order.getShippingPostalCode())
                .build();

        List<OrderItemDto> orderItemDtos = order.getItems().stream()
                .map(item -> OrderItemDto.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .priceAtTime(item.getPriceAtTime())
                        .build()
                )
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .orderStatus(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .address(addressDto)
                .comment(order.getComment())
                .items(orderItemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
