package com.oreo.dto;

import lombok.Data;

@Data
public class FavoriteDto {
    private Long id;
    private Long userId;
    private Long restaurantId;
}