package com.ecommerce.backend.modules.reviews.service;

import com.ecommerce.backend.config.CacheConfig;
import com.ecommerce.backend.modules.auth.jwt.CustomUserDetails;
import com.ecommerce.backend.modules.product.entity.Product;
import com.ecommerce.backend.modules.product.repository.ProductRepository;
import com.ecommerce.backend.modules.reviews.dto.CreateReviewRequest;
import com.ecommerce.backend.modules.reviews.dto.ReviewResponse;
import com.ecommerce.backend.modules.reviews.entity.Review;
import com.ecommerce.backend.modules.reviews.repository.ReviewRepository;
import com.ecommerce.backend.modules.user.entity.User;
import com.ecommerce.backend.modules.user.repository.UserRepository;
import com.ecommerce.backend.shared.exception.BusinessException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_REVIEWS, allEntries = true)
    public ReviewResponse createReview(Long productId, CreateReviewRequest request, CustomUserDetails currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        reviewRepository.findByProductIdAndUserId(productId, user.getId()).ifPresent(review -> {
            throw new BusinessException("You have already reviewed this product", "REVIEW_ALREADY_EXISTS");
        });

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        updateProductRating(product);

        return ReviewResponse.fromEntity(savedReview);
    }

    private void updateProductRating(Product product) {
        double averageRating = reviewRepository.findAverageRatingByProductId(product.getId())
                .orElse(0.0);

        BigDecimal roundedRating = BigDecimal.valueOf(averageRating).setScale(1, RoundingMode.HALF_UP);

        product.setRating(roundedRating);
        productRepository.save(product);
    }

    @Transactional
    @Cacheable(value = CacheConfig.CACHE_REVIEWS, key = "#productId + '_' + #pageable.getPageNumber() + '_' + #pageable.getPageSize()")
    public Page<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw ResourceNotFoundException.product(productId);
        }

        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);

        return reviews.map(ReviewResponse::fromEntity);
    }
}
