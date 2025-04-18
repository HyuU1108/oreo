package com.oreo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RestaurantRequestDto {

    @NotBlank(message = "음식점 이름은 필수입니다.")
    @Size(max = 255, message = "음식점 이름은 최대 255자까지 가능합니다.")
    private String name;

    @Size(max = 500, message = "주소는 최대 500자까지 가능합니다.")
    private String address;

    @Size(max = 20, message = "전화번호는 최대 20자까지 가능합니다.")
    private String phone;

    @Size(max = 2000, message = "설명은 최대 2000자까지 가능합니다.")
    private String description;

    @Size(max = 100, message = "카테고리는 최대 100자까지 가능합니다.")
    private String category;

    @Size(max = 255, message = "이미지 URL/파일명은 최대 255자까지 가능합니다.")
    private String imageUrl;
}