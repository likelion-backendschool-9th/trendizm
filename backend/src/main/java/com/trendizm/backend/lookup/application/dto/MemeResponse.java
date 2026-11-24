package com.trendizm.backend.lookup.application.dto;

import com.trendizm.backend.lookup.domain.entity.Meme;
import java.time.LocalDateTime;

/**
 * 밈 문서. 제목·썸네일은 없으면 null.
 * thumbnail은 저장한 이미지 파일명이다. 화면은 GET /api/lookup/memes/thumbnails/{thumbnail}로 불러오고, null이면 대체 이미지를 보여 준다.
 * 문서를 만든 직후에는 아직 null일 수 있다(썸네일은 커밋 뒤 따로 저장한다).
 * aiSummary는 의견 AI 요약(의견 2개 이상일 때 생김)이고, aiSummaryUpdatedAt은 그 요약을 만든 시각이다. 없으면 둘 다 null.
 */
public record MemeResponse(
        Long id,
        String word,
        String sourceUrl,
        String title,
        String thumbnail,
        String aiSummary,
        LocalDateTime aiSummaryUpdatedAt,
        LocalDateTime createdAt
) {

    public static MemeResponse from(Meme meme) {
        return new MemeResponse(
                meme.getId(),
                meme.getWord(),
                meme.getSourceUrl(),
                meme.getTitle(),
                meme.getThumbnail(),
                meme.getAiSummary(),
                meme.getAiSummaryUpdatedAt(),
                meme.getCreatedAt());
    }
}
