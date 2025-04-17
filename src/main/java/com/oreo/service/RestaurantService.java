package com.oreo.service;

import com.oreo.dto.RestaurantRequestDto;
import com.oreo.dto.RestaurantResponseDto;
import com.oreo.entity.Restaurant;
import com.oreo.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final FileStorageService fileStorageService;
    private final ReviewService reviewService; // ReviewService 주입

    @Transactional
    public RestaurantResponseDto createRestaurant(RestaurantRequestDto requestDto) {
        Restaurant restaurant = Restaurant.builder()
                .name(requestDto.getName())
                .address(requestDto.getAddress())
                .phone(requestDto.getPhone())
                .description(requestDto.getDescription())
                .category(requestDto.getCategory())
                .imageUrl(requestDto.getImageUrl())
                .build();
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        // 생성 시에는 평균 평점 0, 리뷰 수 0으로 DTO 생성
        return new RestaurantResponseDto(savedRestaurant, 0.0, 0);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponseDto> getAllRestaurants(Pageable pageable) {
        Page<Restaurant> restaurantPage = restaurantRepository.findAll(pageable);
        return restaurantPage.map(restaurant ->
            new RestaurantResponseDto(
                restaurant,
                reviewService.getAverageRating(restaurant),
                reviewService.getReviewCount(restaurant)
            )
        );
    }

    @Transactional(readOnly = true)
    public RestaurantResponseDto getRestaurantById(Integer id) {
        Restaurant restaurant = findRestaurantEntityByIdOrThrow(id);
        // 상세 조회 시에도 기본 DTO 생성자 사용 (평점은 컨트롤러에서 별도 조회)
        return new RestaurantResponseDto(restaurant);
    }

    @Transactional(readOnly = true)
    public Restaurant findRestaurantEntityByIdOrThrow(Integer id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID(" + id + ")의 음식점을 찾을 수 없습니다."));
    }

    @Transactional
    public RestaurantResponseDto updateRestaurant(Integer id, RestaurantRequestDto requestDto) {
        Restaurant restaurant = findRestaurantEntityByIdOrThrow(id);
        restaurant.update(
                requestDto.getName(),
                requestDto.getAddress(),
                requestDto.getPhone(),
                requestDto.getDescription(),
                requestDto.getCategory(),
                requestDto.getImageUrl()
        );
        // 수정 시에도 평점/리뷰 수는 별도 계산 필요 (여기서는 기본값 유지)
        return new RestaurantResponseDto(restaurant, reviewService.getAverageRating(restaurant), reviewService.getReviewCount(restaurant));
    }

    @Transactional
    public void deleteRestaurant(Integer id) {
        Restaurant restaurant = findRestaurantEntityByIdOrThrow(id);
        String imageToDelete = restaurant.getImageUrl();
        restaurantRepository.delete(restaurant);
        if (imageToDelete != null && !FileStorageService.isExternalUrl(imageToDelete)) {
             try {
                fileStorageService.deleteFile(imageToDelete);
             } catch (Exception e) {
                 System.err.println("Warning: Failed to delete image file " + imageToDelete + " for restaurant ID " + id + ": " + e.getMessage());
             }
        }
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponseDto> getRestaurantsByCategory(String category, Pageable pageable) {
        Page<Restaurant> restaurantPage = restaurantRepository.findByCategory(category, pageable);
        return restaurantPage.map(restaurant ->
            new RestaurantResponseDto(
                restaurant,
                reviewService.getAverageRating(restaurant),
                reviewService.getReviewCount(restaurant)
            )
        );
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return restaurantRepository.findAllDistinctCategories();
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponseDto> searchRestaurants(String keyword, String searchField, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            // 검색어 없으면 빈 페이지 반환 대신 전체 목록 반환하도록 수정 (선택 사항)
            // return new PageImpl<>(Collections.emptyList(), pageable, 0);
             return getAllRestaurants(pageable); // 혹은 이렇게 변경
        }

        Page<Restaurant> restaurantPage;
        String field = searchField != null ? searchField.toLowerCase() : "all";

        switch (field) {
            case "name":
                restaurantPage = restaurantRepository.findByNameContainingIgnoreCase(keyword, pageable);
                break;
            case "description":
                restaurantPage = restaurantRepository.findByDescriptionContainingIgnoreCase(keyword, pageable);
                break;
            case "all":
            default:
                restaurantPage = restaurantRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);
                break;
        }
        return restaurantPage.map(restaurant ->
            new RestaurantResponseDto(
                restaurant,
                reviewService.getAverageRating(restaurant),
                reviewService.getReviewCount(restaurant)
            )
        );
    }

    @Transactional(readOnly = true)
    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Restaurant findById(Integer id) {
        return findRestaurantEntityByIdOrThrow(id);
    }
}