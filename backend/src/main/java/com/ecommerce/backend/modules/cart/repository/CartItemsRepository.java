package com.ecommerce.backend.modules.cart.repository;

import com.ecommerce.backend.modules.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItem,Long> {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    Optional<CartItem> findByIdAndCartUserId(Long itemId, Long userId);

    void deleteByCartIdAndProductId(Long cartId, Long productId);
}
