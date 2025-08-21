package com.ecommerce.backend.modules.cart.service;

import com.ecommerce.backend.modules.cart.dto.CartItemDto;
import com.ecommerce.backend.modules.cart.dto.CartResponseDto;
import com.ecommerce.backend.modules.cart.dto.UpdateCartItemDto;
import com.ecommerce.backend.modules.cart.entity.Cart;
import com.ecommerce.backend.modules.cart.entity.CartItem;
import com.ecommerce.backend.modules.cart.repository.CartItemsRepository;
import com.ecommerce.backend.modules.cart.repository.CartRepository;
import com.ecommerce.backend.modules.product.entity.Product;
import com.ecommerce.backend.modules.product.repository.ProductRepository;
import com.ecommerce.backend.modules.user.entity.User;
import com.ecommerce.backend.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartItemsRepository cartItemsRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setPrice(BigDecimal.valueOf(100));
        product.setAvailable(true);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
    }

    @Test
    void addItemToCart_whenCartIsEmpty_shouldAddNewItem() {
        // Given
        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setCartId(cart.getId());
        cartItemDto.setProductId(product.getId());
        cartItemDto.setQuantity(2);

        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartItemsRepository.findByCartIdAndProductId(cart.getId(), product.getId())).thenReturn(Optional.empty());

        CartResponseDto result = cartService.addItemToCart(user.getId(), cartItemDto);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProductId()).isEqualTo(product.getId());
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("200");
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addItemToCart_whenItemExists_shouldIncreaseQuantity() {
        CartItem existingItem = new CartItem();
        existingItem.setProduct(product);
        existingItem.setQuantity(1);
        cart.getItems().add(existingItem);

        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setCartId(cart.getId());
        cartItemDto.setProductId(product.getId());
        cartItemDto.setQuantity(2);

        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartItemsRepository.findByCartIdAndProductId(cart.getId(), product.getId())).thenReturn(Optional.of(existingItem));

        CartResponseDto result = cartService.addItemToCart(user.getId(), cartItemDto);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("300");
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void updateCartItem_shouldChangeQuantity() {
        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setProduct(product);
        cartItem.setQuantity(5);
        cart.getItems().add(cartItem);

        UpdateCartItemDto updateDto = new UpdateCartItemDto();
        updateDto.setQuantity(3);

        when(cartItemsRepository.findByIdAndCartUserId(cartItem.getId(), user.getId())).thenReturn(Optional.of(cartItem));
        when(cartRepository.findByUserIdWithItems(user.getId())).thenReturn(Optional.of(cart));

        CartResponseDto result = cartService.updateCartItem(user.getId(), cartItem.getId(), updateDto);

        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("300");
        verify(cartItemsRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void removeItemFromCart_shouldRemoveItem() {
        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);

        Cart cartWithItem = new Cart();
        cartWithItem.setUser(user);
        cartWithItem.getItems().add(cartItem);

        when(cartItemsRepository.findByIdAndCartUserId(cartItem.getId(), user.getId())).thenReturn(Optional.of(cartItem));
        when(cartRepository.findByUserIdWithItems(user.getId())).thenReturn(Optional.of(new Cart()));


        CartResponseDto result = cartService.removeItemFromCart(user.getId(), cartItem.getId());

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalAmount()).isEqualByComparingTo("0");
        verify(cartItemsRepository, times(1)).delete(cartItem);
    }

    @Test
    void clearCart_shouldRemoveAllItems() {
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        cartService.clearCart(user.getId());

        verify(cartRepository, times(1)).save(any(Cart.class));
        assertThat(cart.getItems()).isEmpty();
    }
}
