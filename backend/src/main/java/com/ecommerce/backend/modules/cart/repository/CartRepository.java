package com.ecommerce.backend.modules.cart.repository;

import com.ecommerce.backend.modules.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETC ci.products WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userID") Long userId);

    boolean existsByUserId(Long userId);
}
