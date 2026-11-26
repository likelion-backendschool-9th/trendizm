package com.trendizm.backend.ranking.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 요약 만들기(POST /api/admin/rankings/summaries) 결과. summarized는 저장한 요약, skipped는 저장하지 않은 키워드와 이유다.
 * 수집한 스냅샷이 없으면 collectedAt은 null이고 두 목록은 비어 있다.
 */
public record RankingSummaryResult(
        String platform,
        Long categoryId,
        LocalDateTime collectedAt,
        List<SummarizedKeyword> summarized,
        List<SkippedKeyword> skipped
) {

    public record SummarizedKeyword(Long rankingId, int rank, String keyword, String summary) {
    }

    public record SkippedKeyword(Long rankingId, int rank, String keyword, String reason) {
    }
}
