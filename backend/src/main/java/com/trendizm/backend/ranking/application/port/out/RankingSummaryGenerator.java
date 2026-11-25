package com.trendizm.backend.ranking.application.port.out;

import java.util.List;

/**
 * 키워드와 관련 게시물 글로 AI 요약을 만든다. 구현체는 adapter/out/ai/GeminiRankingSummaryGenerator.
 * 만든 요약은 그대로 저장하지 않고 서비스가 근거를 확인한다.
 */
public interface RankingSummaryGenerator {

    /**
     * 요청 한 번으로 모든 키워드를 요약한다. 키워드마다 답이 정확히 하나씩 있다.
     * 쓸 수 없으면 BusinessException을 던진다: 키 없음 RANKING_SUMMARY_NOT_CONFIGURED,
     * 한도 초과 RANKING_SUMMARY_RATE_LIMITED, 그 밖의 실패 RANKING_SUMMARY_FAILED.
     */
    List<Answer> summarize(List<Target> targets);

    /** 요약할 키워드. posts는 링크 순서(sortOrder)대로다 */
    record Target(Long rankingId, String keyword, List<Post> posts) {
    }

    /** 관련 게시물의 글. 둘 중 하나는 null일 수 있다 */
    record Post(String title, String description) {
    }

    /** evidence는 요약의 근거로 게시물 글에서 인용한 구절. 글만으로 알 수 없으면 둘 다 빈 문자열이다 */
    record Answer(Long rankingId, String summary, String evidence) {
    }
}
