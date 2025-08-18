package com.ecommerce.backend.modules.inventory.repository;

import com.ecommerce.backend.modules.inventory.entity.Inventory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);

    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity > 0")
    List<Inventory> findAllAvailableProducts();

    @Query("SELECT i FROM Inventory i WHERE i.productId IN :productIds")
    List<Inventory> findAllByProductIds(@Param("productIds") List<Long> productIds);

    boolean existsByProductId(Long productId);
}
