package com.oreo.service;

import com.oreo.entity.Favorite;
import com.oreo.entity.Restaurant;
import com.oreo.entity.User;
import com.oreo.repository.FavoriteRepository;
import com.oreo.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<Restaurant> getFavoritesByUser(User user) {
        List<Favorite> favorites = favoriteRepository.findByUser(user);
        return favorites.stream()
                .map(Favorite::getRestaurant)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFavorite(User user, Integer restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with id: " + restaurantId));
        favoriteRepository.deleteByUserAndRestaurant(user, restaurant);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(User user, Restaurant restaurant) {
        return favoriteRepository.findByUserAndRestaurant(user, restaurant).isPresent();
    }

    @Transactional
    public void addFavorite(User user, Integer restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with id: " + restaurantId));

        if (favoriteRepository.findByUserAndRestaurant(user, restaurant).isPresent()) {
            return;
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .restaurant(restaurant)
                .build();

        favoriteRepository.save(favorite);
    }
}