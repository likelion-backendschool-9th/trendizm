package com.trendizm.backend.item.application.dto;

import com.trendizm.backend.item.domain.Item;

/** 컬럼이 정해지면 응답 필드를 추가한다. */
public record ItemResponse(Long id) {

    public static ItemResponse from(Item item) {
        return new ItemResponse(item.getId());
    }
}
