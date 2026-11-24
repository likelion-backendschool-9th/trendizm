package com.trendizm.backend.ranking.adapter.in.web;

import com.trendizm.backend.ranking.application.dto.RankingImportResult;
import com.trendizm.backend.ranking.application.port.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 트렌드 순위 CSV 업로드 (관리자). /api/admin/**은 ADMIN만 쓸 수 있다 (auth/adapter/out/security/SecurityConfig).
 * 브라우저는 Next 서버(BFF)를 거쳐 부르고, 본문은 CSV 파일 내용 그대로 Content-Type: text/csv로 보낸다.
 * multipart(파일 업로드 폼)로 받지 않는다. multipart 파싱은 JDK Base64 디코더를 거친다 (AGENTS.md Java4Shell).
 * CSV 형식은 python/ranking/instagram/instagram_keyword_pipeline.py가 만드는 ranking_instagram.csv다.
 */
@RestController
@RequestMapping("/api/admin/rankings")
@RequiredArgsConstructor
public class RankingAdminController {

    private final RankingService rankingService;

    /** 바이트로 받아 UTF-8로 직접 읽는다. Content-Type에 charset이 없어도 한글이 깨지지 않는다 */
    @PostMapping(consumes = "text/csv")
    @ResponseStatus(HttpStatus.CREATED)
    public RankingImportResult importRankings(@RequestBody byte[] csv) {
        return rankingService.importRankings(RankingCsvReader.read(csv));
    }
}
