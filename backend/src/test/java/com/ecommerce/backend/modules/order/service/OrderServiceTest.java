package com.ecommerce.backend.modules.order.service;

import com.ecommerce.backend.modules.cart.entity.Cart;
import com.ecommerce.backend.modules.cart.entity.CartItem;
import com.ecommerce.backend.modules.cart.repository.CartRepository;
import com.ecommerce.backend.modules.cart.service.CartService;
import com.ecommerce.backend.modules.inventory.service.InventoryService;
import com.ecommerce.backend.modules.order.dto.CreateOrderRequest;
import com.ecommerce.backend.modules.order.dto.OrderStatusUpdateRequest;
import com.ecommerce.backend.modules.order.entity.Order;
import com.ecommerce.backend.modules.order.entity.OrderStatus;
import com.ecommerce.backend.modules.order.repository.OrdersRepository;
import com.ecommerce.backend.modules.product.entity.Product;
import com.ecommerce.backend.modules.product.repository.ProductRepository;
import com.ecommerce.backend.modules.user.entity.User;
import com.ecommerce.backend.modules.user.repository.UserRepository;
import com.ecommerce.backend.shared.dto.AddressDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrdersRepository ordersRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private CartService cartService;
    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private Product product;
    private String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail(userEmail);

        product = new Product();
        product.setId(10L);
        product.setAvailable(true);
        product.setPrice(BigDecimal.TEN);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart = new Cart();
        cart.setUser(user);
        cart.getItems().add(cartItem);
    }

    @Test
    void createOrder_shouldReserveInventory() {
        CreateOrderRequest request = new CreateOrderRequest(new AddressDto(), "comment");
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserIdWithItems(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        when(ordersRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order orderToSave = invocation.getArgument(0);
            orderToSave.setId(1L);
            return orderToSave;
        });

        orderService.createOrder(userEmail, request);

        verify(inventoryService).reserveProduct(eq(1L), any(Map.class), eq(userEmail));
        verify(cartService).clearCart(user.getId());
    }

    @Test
    void closeOrder_shouldReleaseInventory() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(ordersRepository.findByIdAndUser(order.getId(), user)).thenReturn(Optional.of(order));
        when(ordersRepository.save(any(Order.class))).thenReturn(order);

        orderService.closeOrder(userEmail, order.getId());

        verify(inventoryService).cancelReservation(order.getId(), userEmail);
    }

    @Test
    void updateOrderStatus_toShipped_shouldConfirmReservation() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.COMFIRMED);
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setStatus(OrderStatus.SHIPPED);

        when(ordersRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(ordersRepository.save(any(Order.class))).thenReturn(order);

        orderService.updateOrderStatus(order.getId(), request, userEmail);

        verify(inventoryService).confirmReservation(order.getId(), userEmail);
        verify(inventoryService, never()).cancelReservation(anyLong(), anyString());
    }

    @Test
    void updateOrderStatus_toCancelled_shouldCancelReservation() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setStatus(OrderStatus.CANCELLED);

        when(ordersRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(ordersRepository.save(any(Order.class))).thenReturn(order);

        orderService.updateOrderStatus(order.getId(), request, userEmail);

        verify(inventoryService).cancelReservation(order.getId(), userEmail);
        verify(inventoryService, never()).confirmReservation(anyLong(), anyString());
    }
}
