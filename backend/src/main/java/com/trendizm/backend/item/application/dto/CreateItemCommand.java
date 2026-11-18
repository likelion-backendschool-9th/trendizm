package com.trendizm.backend.item.application.dto;

import java.util.List;

/** 추천 아이템 등록 */
public record CreateItemCommand(
        Long questionId,
        Long userId,
        String title,
        String seller,
        Integer price,
        List<String> tags,
        String description,
        String imageUrl,
        String linkUrl
) {
}
