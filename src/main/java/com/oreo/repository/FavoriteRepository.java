package com.oreo.repository;

import com.oreo.entity.Favorite;
import com.oreo.entity.Restaurant;
import com.oreo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUser(User user);

    Optional<Favorite> findByUserAndRestaurant(User user, Restaurant restaurant);

    void deleteByUserAndRestaurant(User user, Restaurant restaurant);
}