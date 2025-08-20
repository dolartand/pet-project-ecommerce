package com.ecommerce.backend.modules.reviews.repository;

import com.ecommerce.backend.modules.reviews.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductId(Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Optional<Double> findAverageRatingByProductId(Long productId);

    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);
}
