package com.trendizm.backend.lookup.application.dto;

import com.trendizm.backend.lookup.domain.Meme;

/** 컬럼이 정해지면 응답 필드를 추가한다. */
public record MemeResponse(Long id) {

    public static MemeResponse from(Meme meme) {
        return new MemeResponse(meme.getId());
    }
}
