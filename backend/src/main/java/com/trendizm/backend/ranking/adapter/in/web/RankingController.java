package com.trendizm.backend.ranking.adapter.in.web;

import com.trendizm.backend.ranking.adapter.in.web.dto.RankingRequest;
import com.trendizm.backend.ranking.application.dto.RankingResponse;
import com.trendizm.backend.ranking.application.port.RankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 없이 모든 요청 허용 (auth/adapter/out/security/SecurityConfig) */
@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public Page<RankingResponse> getRankings(Pageable pageable) {
        return rankingService.getRankings(pageable);
    }

    @GetMapping("/{rankingId}")
    public RankingResponse getRanking(@PathVariable Long rankingId) {
        return rankingService.getRanking(rankingId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createRanking(@Valid @RequestBody RankingRequest request) {
        return rankingService.createRanking();
    }

    @PutMapping("/{rankingId}")
    public void updateRanking(@PathVariable Long rankingId, @Valid @RequestBody RankingRequest request) {
        rankingService.updateRanking(rankingId);
    }

    @DeleteMapping("/{rankingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRanking(@PathVariable Long rankingId) {
        rankingService.deleteRanking(rankingId);
    }
}
