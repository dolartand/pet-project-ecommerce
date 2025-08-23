package com.ecommerce.backend.modules.cart.repository;

import com.ecommerce.backend.modules.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT c FROM Cart c JOIN FETCH c.user WHERE c.updated_at BETWEEN :startTime AND :endTime AND c.items IS NOT EMPTY")
    List<Cart> findAbandonedCarts(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}