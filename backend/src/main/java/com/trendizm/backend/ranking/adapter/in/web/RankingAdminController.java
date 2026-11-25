package com.trendizm.backend.ranking.adapter.in.web;

import com.trendizm.backend.ranking.application.dto.RankingImportResult;
import com.trendizm.backend.ranking.application.dto.RankingSummaryResult;
import com.trendizm.backend.ranking.application.port.RankingService;
import com.trendizm.backend.ranking.application.port.RankingSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 트렌드 순위 CSV 업로드와 AI 요약 만들기 (관리자). /api/admin/**은 ADMIN만 쓸 수 있다 (auth/adapter/out/security/SecurityConfig).
 * 브라우저는 Next 서버(BFF)를 거쳐 부르고, 본문은 CSV 파일 내용 그대로 Content-Type: text/csv로 보낸다.
 * multipart(파일 업로드 폼)로 받지 않는다. multipart 파싱은 JDK Base64 디코더를 거친다 (AGENTS.md Java4Shell).
 * CSV 형식은 python/ranking/instagram/instagram_keyword_pipeline.py가 만드는 ranking_instagram.csv다.
 */
@RestController
@RequestMapping("/api/admin/rankings")
@RequiredArgsConstructor
public class RankingAdminController {

    private final RankingService rankingService;
    private final RankingSummaryService summaryService;

    /** 바이트로 받아 UTF-8로 직접 읽는다. Content-Type에 charset이 없어도 한글이 깨지지 않는다 */
    @PostMapping(consumes = "text/csv")
    @ResponseStatus(HttpStatus.CREATED)
    public RankingImportResult importRankings(@RequestBody byte[] csv) {
        return rankingService.importRankings(RankingCsvReader.read(csv));
    }

    /**
     * 가장 최근 스냅샷(1~10위)의 키워드를 관련 링크의 제목·설명으로 요약해 저장한다. AI 응답을 기다리므로 최대 1분쯤 걸린다.
     * 예: POST /api/admin/rankings/summaries?platform=instagram&amp;category=challenge
     * 이미 요약이 있는 키워드는 건너뛰고, overwrite=true면 다시 만든다
     */
    @PostMapping("/summaries")
    public RankingSummaryResult summarize(@RequestParam String platform, @RequestParam String category,
                                          @RequestParam(defaultValue = "false") boolean overwrite) {
        return summaryService.summarizeLatest(platform, category, overwrite);
    }
}
