package com.oreo.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Long id;
    private String content;
    private Integer rating;
    private String nickname; // 작성자 표시용
    private String createdAt;
}