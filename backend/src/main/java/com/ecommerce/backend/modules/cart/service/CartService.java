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
import com.ecommerce.backend.shared.exception.BusinessException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemsRepository cartItemsRepository;

    @Transactional
    public CartResponseDto getUserCart(Long userId) {
        log.info("Fetching cart for user with id: {}", userId);
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createEmptyCart(userId));

        log.info("Successfully fetched cart for user with id: {}", userId);
        return mapToCartResponseDto(cart);
    }

    @Transactional
    public CartResponseDto addItemToCart(Long userId, CartItemDto cartItemDto) {
        log.info("Attempting to add item to cart for user with id: {}. Item: {}", userId, cartItemDto);
        if (cartItemDto.getProductId() == null) {
            log.error("Cannot add item with null id for user with id: {}", userId);
            throw new BusinessException("Product ID must not be null", "PRODUCT_ID_NULL");
        }

        Cart cart = getOrCreateCart(userId);
        Product product = getProduct(cartItemDto.getProductId());

        validateProductAvailability(product);

        Optional<CartItem> existingItem = cartItemsRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            log.info("Updating quantity for existing item {} in cart for user {}", cartItem.getId(), userId);
            cartItem.setQuantity(cartItem.getQuantity() + cartItemDto.getQuantity());
        } else {
            log.info("Adding new item to cart for user {}", userId);
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(cartItemDto.getQuantity());

            cart.getItems().add(newItem);
        }
        cartRepository.save(cart);
        log.info("Successfully added item to cart for user with id: {}", userId);
        return mapToCartResponseDto(cart);
    }

    @Transactional
    public CartResponseDto updateCartItem(Long userId, Long itemId, UpdateCartItemDto dto) {
        log.info("Updating item with id: {} in cart for user with id: {}", itemId, userId);
        CartItem cartItem = cartItemsRepository.findByIdAndCartUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem is not found"));
        cartItem.setQuantity(dto.getQuantity());
        cartItemsRepository.save(cartItem);

        Cart cart = cartRepository.findByUserIdWithItems(userId).orElseThrow();
        log.info("Successfully updated item in cart for user with id: {}", userId);
        return mapToCartResponseDto(cart);
    }

    @Transactional
    public CartResponseDto removeItemFromCart(Long userId, Long itemId) {
        log.info("Removing item with id: {} from cart for user with id: {}", itemId, userId);
        CartItem cartItem = cartItemsRepository.findByIdAndCartUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Элемент корзины не найден"));

        cartItemsRepository.delete(cartItem);

        Cart cart = cartRepository.findByUserIdWithItems(userId).orElseThrow();
        log.info("Successfully removed item from cart for user with id: {}", userId);
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

    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user with id: {}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart is not found"));
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Successfully cleared cart for user with id: {}", userId);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));
    }

    protected Cart createEmptyCart(Long userId) {
        log.info("Creating an empty cart for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not found"));

        Cart cart = new Cart();
        cart.setUser(user);
        Cart savedCart = cartRepository.save(cart);
        log.info("Successfully created an empty cart for user {}", userId);
        return savedCart;
    }

    private Product getProduct(Long productId) {
        log.debug("Fetching product with id: {}", productId);
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found with id {}", productId);
                    return new ResourceNotFoundException("Product is not found");
                });
    }

    private void validateProductAvailability(Product product) {
        log.debug("Validating availability for product {}", product.getId());
        if (!product.getAvailable()) {
            log.warn("Product {} is not available for order", product.getId());
            throw new BusinessException("Product is not available for order", "PRODUCT_NOT_AVAILABLE");
        }
    }
}
