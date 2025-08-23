package com.ecommerce.backend.modules.reviews.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Product product;
    private User user;
    private CustomUserDetails customUserDetails;
    private CreateReviewRequest createReviewRequest;
    private Review review;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);

        user = new User();
        user.setId(1L);

        customUserDetails = CustomUserDetails.builder().id(1L).build();

        createReviewRequest = new CreateReviewRequest();
        createReviewRequest.setRating(5);
        createReviewRequest.setComment("Great product!");

        review = Review.builder()
                .id(1L)
                .product(product)
                .user(user)
                .rating(5)
                .comment("Great!")
                .build();
    }

    @Test
    void createReview_whenValid_shouldCreateAndReturnReview() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewRepository.findByProductIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewRepository.findAverageRatingByProductId(1L)).thenReturn(Optional.of(4.5));

        ReviewResponse response = reviewService.createReview(1L, createReviewRequest, customUserDetails);

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        verify(productRepository).save(product);
        assertThat(product.getRating()).isEqualByComparingTo("4.5");
    }

    @Test
    void createReview_whenProductNotFound_shouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(1L, createReviewRequest, customUserDetails))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createReview_whenUserAlreadyReviewed_shouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewRepository.findByProductIdAndUserId(1L, 1L)).thenReturn(Optional.of(new Review()));

        assertThatThrownBy(() -> reviewService.createReview(1L, createReviewRequest, customUserDetails))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getReviewsByProductId_whenProductExists_shouldReturnReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review), pageable, 1);

        when(productRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByProductId(1L, pageable)).thenReturn(reviewPage);

        Page<ReviewResponse> responsePage = reviewService.getReviewsByProductId(1L, pageable);

        assertThat(responsePage.getTotalElements()).isEqualTo(1);
        assertThat(responsePage.getContent().get(0).getComment()).isEqualTo("Great!");
    }

    @Test
    void getReviewsByProductId_whenProductDoesNotExist_shouldThrowException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.getReviewsByProductId(99L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
