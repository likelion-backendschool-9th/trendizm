package com.trendizm.backend.lookup.application.dto;

import com.trendizm.backend.lookup.domain.entity.Meme;
import java.time.LocalDateTime;

/** 밈 문서. 출처 값(sourceUrl, title, thumbnailUrl)은 없으면 null */
public record MemeResponse(
        Long id,
        String word,
        String sourceUrl,
        String title,
        String thumbnailUrl,
        LocalDateTime createdAt
) {

    public static MemeResponse from(Meme meme) {
        return new MemeResponse(
                meme.getId(),
                meme.getWord(),
                meme.getSourceUrl(),
                meme.getTitle(),
                meme.getThumbnailUrl(),
                meme.getCreatedAt());
    }
}
