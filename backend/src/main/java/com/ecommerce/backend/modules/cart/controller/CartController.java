package com.ecommerce.backend.modules.cart.controller;

import com.ecommerce.backend.modules.cart.dto.CartItemDto;
import com.ecommerce.backend.modules.cart.dto.CartResponseDto;
import com.ecommerce.backend.modules.cart.dto.UpdateCartItemDto;
import com.ecommerce.backend.modules.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDto> getCart(Authentication authentication) {
        Long userId = getUserFromAuthentication(authentication);
        CartResponseDto cart = cartService.getUserCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItem(
            @Valid @RequestBody CartItemDto dto,
            Authentication authentication
    ) {
        Long userId = getUserFromAuthentication(authentication);
        CartResponseDto cart = cartService.addItemToCart(userId, dto);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDto> updateItem (
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemDto dto,
            Authentication authentication
    ) {
        Long userId = getUserFromAuthentication(authentication);
        CartResponseDto cart = cartService.updateCartItem(userId, itemId, dto);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDto> deleteItem (
            @PathVariable Long itemId,
            Authentication authentication
    ) {
        Long userId = getUserFromAuthentication(authentication);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    // TODO: реализовать извлечение айди из токена (использовать вместо аутентификации токен)
    private Long getUserFromAuthentication(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
