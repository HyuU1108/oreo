package com.oreo.repository;

import com.oreo.entity.Restaurant;
import com.oreo.entity.Review;
import com.oreo.entity.User; // User 파라미터 사용 시 필요
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRestaurantOrderByCreatedAtDesc(Restaurant restaurant);

    List<Review> findByRestaurant(Restaurant restaurant);

    int countByRestaurant(Restaurant restaurant);

    Optional<Review> findByIdAndUser(Long id, User user);
}