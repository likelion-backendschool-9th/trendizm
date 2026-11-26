package com.trendizm.backend.ranking.application.dto;

import com.trendizm.backend.ranking.domain.entity.TrendRanking;
import java.util.List;

/**
 * 순위 키워드 하나. summary는 AI 요약을 만들기 전이거나 만들지 못했으면 null이다.
 * 관련 링크는 서비스에서 채운다: from(ranking).withLinks(...)
 */
public record RankingResponse(
        Long id,
        int rank,
        String keyword,
        String summary,
        List<RankingLinkResponse> links
) {

    public static RankingResponse from(TrendRanking ranking) {
        return new RankingResponse(ranking.getId(), ranking.getRank(), ranking.getKeyword(), ranking.getSummary(),
                List.of());
    }

    public RankingResponse withLinks(List<RankingLinkResponse> links) {
        return new RankingResponse(id, rank, keyword, summary, List.copyOf(links));
    }
}
