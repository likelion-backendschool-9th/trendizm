package com.trendizm.backend.ranking.adapter.in.web;

import com.trendizm.backend.ranking.application.dto.RankingSnapshot;
import com.trendizm.backend.ranking.application.dto.RankingTabs;
import com.trendizm.backend.ranking.application.port.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 트렌드 순위 조회 (WBS 5.4). 조회(GET)는 로그인 없이 허용한다 (auth/adapter/out/security/SecurityConfig).
 * 순위는 수집 스케줄러(5.2)가 저장하므로 등록·수정·삭제 API는 두지 않는다.
 */
@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 플랫폼·카테고리의 가장 최근 스냅샷. 예: GET /api/rankings?platform=instagram&amp;category=challenge
     * platform과 category는 slug(GET /api/rankings/tabs)다. 없는 값이면 404
     */
    @GetMapping
    public RankingSnapshot getRankings(@RequestParam String platform, @RequestParam String category) {
        return rankingService.getLatestSnapshot(platform, category);
    }

    /**
     * 순위 화면의 탭. 플랫폼(slug·label)과 카테고리(id·slug·title)를 함께 준다.
     * 화면은 이 목록으로 플랫폼·카테고리 메뉴를 만들고, 순위 조회에는 두 slug를 쓴다
     */
    @GetMapping("/tabs")
    public RankingTabs getTabs() {
        return rankingService.getTabs();
    }
}
