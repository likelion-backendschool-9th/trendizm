package com.trendizm.backend.ranking.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 순위 CSV 업로드(POST /api/admin/rankings) 결과.
 * skipped는 저장하지 않은 줄(DB에 없는 카테고리, 순위가 빈 줄), merged는 같은 카테고리에서 하나로 합친 키워드다.
 */
public record RankingImportResult(
        LocalDateTime collectedAt,
        int savedRankingCount,
        int savedLinkCount,
        List<SkippedRow> skipped,
        List<MergedKeyword> merged
) {

    /** 저장하지 않은 줄과 그 이유 */
    public record SkippedRow(int line, String category, String keyword, String reason) {
    }

    /**
     * 대소문자만 다르거나 완전히 같아서 하나로 합친 키워드. keyword는 저장한 형태이고,
     * lines는 합친 줄 번호이며 처음 줄이 남긴 줄이다(순위가 높은 줄, 순위가 같으면 위쪽 줄).
     */
    public record MergedKeyword(String category, String keyword, List<Integer> lines) {
    }
}
