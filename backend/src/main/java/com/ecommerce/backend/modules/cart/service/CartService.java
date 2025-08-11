package com.ecommerce.backend.modules.cart.service;

import com.ecommerce.backend.modules.cart.dto.CartItemDto;
import com.ecommerce.backend.modules.cart.dto.CartItemResponseDto;
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
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemsRepository cartItemsRepository;

    @Transactional(readOnly = true)
    public CartResponseDto getUserCart(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createEmptyCart(userId));

        return mapToCartResponseDto(cart);
    }

    public CartResponseDto addItemToCart(Long userId, CartItemDto cartItemDto) {
        Cart cart = getOrCreateCart(userId);
        Product product = getProduct(cartItemDto.getProductId());

        validateProductAvailability(product);

        Optional<CartItem> existingItem = cartItemsRepository.findByCartIdAndProductId(cartItemDto.getCartId(), product.getId());

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + cartItemDto.getQuantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(cartItemDto.getQuantity());

            cart.getItems().add(newItem);
        }
        cartRepository.save(cart);
        return mapToCartResponseDto(cart);
    }

    public CartResponseDto updateCartItem(Long userId, Long itemId, UpdateCartItemDto dto) {
        CartItem cartItem = cartItemsRepository.findByIdAndCartUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem is not found"));
        cartItem.setQuantity(dto.getQuantity());
        cartItemsRepository.save(cartItem);

        Cart cart = cartRepository.findByUserIdWithItems(userId).orElseThrow();
        return mapToCartResponseDto(cart);
    }

    public CartResponseDto removeItemFromCart(Long userId, Long itemId) {
        CartItem cartItem = cartItemsRepository.findByIdAndCartUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Элемент корзины не найден"));

        cartItemsRepository.delete(cartItem);

        Cart cart = cartRepository.findByUserIdWithItems(userId).orElseThrow();
        return mapToCartResponseDto(cart);
    }

    private CartResponseDto mapToCartResponseDto(Cart cart) {
        CartResponseDto cartResponseDto = new CartResponseDto();
        List<CartItemResponseDto> itemDtos = cart.getItems().stream()
                .map(this::mapToCartItemResponseDto)
                .toList();
        cartResponseDto.setItems(itemDtos);
        cartResponseDto.setTotalAmount(calculateTotalAmount(itemDtos));
        return cartResponseDto;
    }

    private CartItemResponseDto mapToCartItemResponseDto(CartItem cartItem) {
        CartItemResponseDto dto = new CartItemResponseDto();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setQuantity(cartItem.getQuantity());
        dto.setImageUrl(cartItem.getProduct().getImageUrl());
        dto.setPrice(cartItem.getProduct().getPrice());

        return dto;
    }

    private BigDecimal calculateTotalAmount(List<CartItemResponseDto> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart is not found"));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private void createCartIfNotExists(Long userId) {
        if (!cartRepository.existsByUserId(userId)) {
            createEmptyCart(userId);
        }
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));
    }

    private Cart createEmptyCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not found"));

        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product is not found"));
    }

    private void validateProductAvailability(Product product) {
        if (!product.getAvailable()) {
            throw new IllegalArgumentException("Product is not available");
        }
    }
}
