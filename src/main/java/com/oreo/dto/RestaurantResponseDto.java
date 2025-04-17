package com.oreo.dto;

import com.oreo.entity.Restaurant;
import lombok.Getter;

@Getter
public class RestaurantResponseDto {
    private Integer id;
    private String name;
    private String address;
    private String phone;
    private String description;
    private String category;
    private String imageUrl;
    private double averageRating;
    private int reviewCount;

    // 상세 보기 등에서 사용 (평점 계산 X)
    public RestaurantResponseDto(Restaurant entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.address = entity.getAddress();
        this.phone = entity.getPhone();
        this.description = entity.getDescription();
        this.category = entity.getCategory();
        this.imageUrl = entity.getImageUrl();
        this.averageRating = 0.0; // 기본값 설정
        this.reviewCount = 0; // 기본값 설정
    }

    // 목록 조회 시 사용 (평점/리뷰 수 포함)
    public RestaurantResponseDto(Restaurant entity, double averageRating, int reviewCount) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.address = entity.getAddress();
        this.phone = entity.getPhone();
        this.category = entity.getCategory();
        this.imageUrl = entity.getImageUrl();
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }
}