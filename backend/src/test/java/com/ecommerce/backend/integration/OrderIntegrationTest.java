package com.ecommerce.backend.integration;

import com.ecommerce.backend.modules.cart.dto.CartItemDto;
import com.ecommerce.backend.modules.cart.service.CartService;
import com.ecommerce.backend.modules.category.entity.Category;
import com.ecommerce.backend.modules.category.repository.CategoryRepository;
import com.ecommerce.backend.modules.inventory.entity.Inventory;
import com.ecommerce.backend.modules.inventory.repository.InventoryRepository;
import com.ecommerce.backend.modules.order.dto.CreateOrderRequest;
import com.ecommerce.backend.modules.order.dto.OrderDto;
import com.ecommerce.backend.modules.order.entity.OrderStatus;
import com.ecommerce.backend.modules.order.service.OrderService;
import com.ecommerce.backend.modules.product.entity.Product;
import com.ecommerce.backend.modules.product.repository.ProductRepository;
import com.ecommerce.backend.modules.user.entity.User;
import com.ecommerce.backend.modules.user.entity.UserRole;
import com.ecommerce.backend.modules.user.repository.UserRepository;
import com.ecommerce.backend.shared.dto.AddressDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class OrderIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldCorrectlyProcessOrderLifecycle()  {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("pass");
        user.setRole(UserRole.USER);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCreatedAt(java.time.LocalDateTime.now());
        user = userRepository.save(user);

        Category category = new Category();
        category.setName("Test Category");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("test prod");
        product.setCategoryId(category.getId());
        product.setAvailable(true);
        product.setImageUrl("test img");
        product.setPrice(BigDecimal.valueOf(10));
        product.setCreatedAt(java.time.LocalDateTime.now());
        product = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);
        inventory.setCreatedAt(java.time.LocalDateTime.now());
        inventory.setUpdatedAt(java.time.LocalDateTime.now());
        inventoryRepository.save(inventory);

        CartItemDto itemDto = new CartItemDto();
        itemDto.setProductId(product.getId());
        itemDto.setQuantity(3);
        cartService.addItemToCart(user.getId(), itemDto);

        CreateOrderRequest request = new CreateOrderRequest(new AddressDto("testStreet", "testCity", "1234"), "Integration test");
        OrderDto createdOrder = orderService.createOrder(user.getEmail(), request);
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);

        Inventory inventoryAfterReserve = inventoryRepository.findById(product.getId()).orElseThrow();
        assertThat(inventoryAfterReserve.getAvailableQuantity()).isEqualTo(7);
        assertThat(inventoryAfterReserve.getReservedQuantity()).isEqualTo(3);

        OrderDto cancelledOrder = orderService.closeOrder(user.getEmail(), createdOrder.getId());
        assertThat(cancelledOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

        Inventory inventoryAfterCancel = inventoryRepository.findById(product.getId()).orElseThrow();
        assertThat(inventoryAfterCancel.getAvailableQuantity()).isEqualTo(10);
        assertThat(inventoryAfterCancel.getReservedQuantity()).isEqualTo(0);
    }
}
