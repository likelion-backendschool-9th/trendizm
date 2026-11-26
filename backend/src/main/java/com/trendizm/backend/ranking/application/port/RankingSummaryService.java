package com.trendizm.backend.ranking.application.port;

import com.trendizm.backend.ranking.application.dto.RankingSummaryResult;

/** 순위 키워드의 AI 요약. 관련 링크(trend_ranking_links)의 제목·설명을 읽고 만들어 trend_rankings.summary에 저장한다 */
public interface RankingSummaryService {

    /**
     * 플랫폼·카테고리의 가장 최근 스냅샷(1~10위) 키워드를 요약해 저장한다. platform과 category는 slug다.
     * overwrite가 false면 이미 요약이 있는 키워드는 건너뛴다. 만들지 못한 키워드는 원래 값을 바꾸지 않고 이유를 알린다.
     * 없는 플랫폼·카테고리(미분류 포함)이거나 AI를 쓸 수 없으면 예외를 던지고 아무것도 저장하지 않는다.
     */
    RankingSummaryResult summarizeLatest(String platform, String category, boolean overwrite);
}
