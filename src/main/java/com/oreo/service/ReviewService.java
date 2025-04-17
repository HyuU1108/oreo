package com.oreo.service;

import com.oreo.entity.Restaurant;
import com.oreo.entity.Review;
import com.oreo.entity.User;
import com.oreo.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public void addReview(User user, Restaurant restaurant, String content, int rating) {
        Review review = Review.builder()
                .user(user)
                .restaurant(restaurant)
                .content(content)
                .rating(rating)
                .build();
        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsByRestaurant(Restaurant restaurant) {
        return reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant);
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new SecurityException("리뷰 삭제 권한이 없습니다. User ID: " + user.getId() + ", Review ID: " + reviewId);
        }
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Restaurant restaurant) {
        return reviewRepository.findByRestaurant(restaurant).stream()
                      .mapToInt(Review::getRating)
                      .average()
                      .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public int getReviewCount(Restaurant restaurant) {
        return reviewRepository.countByRestaurant(restaurant);
    }
}