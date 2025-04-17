package com.oreo.repository;

import com.oreo.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {

    Page<Restaurant> findByCategory(String category, Pageable pageable);

    Page<Restaurant> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameKeyword, String descriptionKeyword, Pageable pageable);

    Page<Restaurant> findByNameContainingIgnoreCase(String nameKeyword, Pageable pageable);

    Page<Restaurant> findByDescriptionContainingIgnoreCase(String descriptionKeyword, Pageable pageable);

    @Query("SELECT DISTINCT r.category FROM Restaurant r WHERE r.category IS NOT NULL AND r.category <> '' ORDER BY r.category")
    List<String> findAllDistinctCategories();
}