package com.ecommerce.backend.modules.reviews.controller;

import com.ecommerce.backend.modules.auth.jwt.CustomUserDetails;
import com.ecommerce.backend.modules.reviews.dto.CreateReviewRequest;
import com.ecommerce.backend.modules.reviews.dto.ReviewResponse;
import com.ecommerce.backend.modules.reviews.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product/{productId}/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails  currentUser
    ) {
        log.info("User {} creating review for product {}: {}", currentUser.getUsername(), productId, request);
        ReviewResponse response = reviewService.createReview(productId, request, currentUser);
        log.info("Successfully created review for product {}. Review id: {}", productId, response.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviewsByProductId(
            @PathVariable Long productId,
            Pageable pageable
    ) {
        log.info("Fetching reviews for product {}. Pageable: {}", productId, pageable);
        Page<ReviewResponse> reviews = reviewService.getReviewsByProductId(productId,  pageable);
        log.info("Successfully fetched {} reviews for product {}", reviews.getTotalElements(), productId);
        return ResponseEntity.ok(reviews);
    }
}
