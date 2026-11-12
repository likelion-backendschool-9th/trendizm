package com.trendizm.backend.ranking.application.dto;

import com.trendizm.backend.ranking.domain.Ranking;

/** 컬럼이 정해지면 응답 필드를 추가한다. */
public record RankingResponse(Long id) {

    public static RankingResponse from(Ranking ranking) {
        return new RankingResponse(ranking.getId());
    }
}
