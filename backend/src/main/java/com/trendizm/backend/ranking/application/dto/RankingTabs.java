package com.trendizm.backend.ranking.application.dto;

import com.trendizm.backend.ranking.domain.enums.Platform;
import java.util.List;

/**
 * GET /api/rankings/tabs 응답. 순위 화면의 플랫폼 탭과 카테고리 탭이다.
 * platforms는 Platform enum 선언 순서, categories는 미분류를 뺀 id 순이다.
 * 순위 조회(GET /api/rankings)의 platform에는 platforms의 slug를, category에는 categories의 slug를 쓴다.
 * 엔티티 하나로 만드는 응답이 아니라서 XxxResponse.from(entity) 규칙(ArchitectureTest) 대상이 아니도록 이름을 Response로 끝내지 않는다.
 */
public record RankingTabs(
        List<PlatformTab> platforms,
        List<RankingCategoryResponse> categories
) {

    /** 플랫폼 탭. slug는 화면 주소와 순위 조회의 platform 값이고, label은 탭에 보여 줄 이름이다. 예: youtube, YouTube */
    public record PlatformTab(String slug, String label) {

        public static PlatformTab from(Platform platform) {
            return new PlatformTab(platform.name(), platform.getLabel());
        }
    }
}
