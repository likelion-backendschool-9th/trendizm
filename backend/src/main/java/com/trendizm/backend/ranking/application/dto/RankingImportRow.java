package com.trendizm.backend.ranking.application.dto;

import java.util.List;

/**
 * 순위 CSV(POST /api/admin/rankings)의 한 줄. 어댑터가 CSV와 JSON 배열 칸을 읽어 만들고, 값이 맞는지는 서비스가 검사한다.
 * line은 파일에서 이 줄이 시작하는 줄 번호다(헤더가 1). 칸 값은 CSV에 적힌 그대로다.
 * postIds·postAuthors·postTitles는 related_posts·related_posts_author·related_posts_title 칸의 JSON 배열이다.
 * 칸이 비어 있으면 빈 목록이고, 배열 안의 null은 그대로 둔다.
 */
public record RankingImportRow(
        int line,
        String platform,
        String category,
        String ranking,
        String keyword,
        List<String> postIds,
        List<String> postAuthors,
        List<String> postTitles,
        String collectedAt,
        String startedAt
) {
}
