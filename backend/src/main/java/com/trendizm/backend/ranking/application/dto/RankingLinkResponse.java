package com.trendizm.backend.ranking.application.dto;

import com.trendizm.backend.ranking.domain.entity.TrendRankingLink;
import java.time.LocalDateTime;

/** 순위 키워드의 관련 링크. thumbnailUrl은 플랫폼 정책상 표시할 수 없으면 null이다 */
public record RankingLinkResponse(
        Long id,
        String title,
        String url,
        String thumbnailUrl,
        String description,
        LocalDateTime publishedAt
) {

    public static RankingLinkResponse from(TrendRankingLink link) {
        return new RankingLinkResponse(link.getId(), link.getTitle(), link.getUrl(), link.getThumbnail(),
                link.getDescription(), link.getPublishedAt());
    }
}
