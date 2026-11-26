package com.trendizm.backend.ranking.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /api/rankings 응답. 플랫폼·카테고리의 가장 최근 스냅샷 하나이고, rankings는 순위 순서다.
 * platform은 slug(예: youtube), categoryId는 GET /api/rankings/tabs의 카테고리 id다. 이름은 화면에서 그 목록과 맞춰 보여 준다.
 * startedAt은 집계 기간의 시작이고 없을 수 있다. 아직 수집한 스냅샷이 없으면 startedAt·collectedAt은 null, rankings는 빈 목록이다.
 * 엔티티 하나로 만드는 응답이 아니라서 XxxResponse.from(entity) 규칙(ArchitectureTest) 대상이 아니도록 이름을 Response로 끝내지 않는다.
 */
public record RankingSnapshot(
        String platform,
        Long categoryId,
        LocalDateTime startedAt,
        LocalDateTime collectedAt,
        List<RankingResponse> rankings
) {
}
