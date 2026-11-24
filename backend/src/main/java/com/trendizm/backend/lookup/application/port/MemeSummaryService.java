package com.trendizm.backend.lookup.application.port;

/**
 * 밈 문서의 의견 AI 요약. 의견이나 투표가 바뀌면 커밋 뒤에 따로(비동기) 다시 만들어 memes.ai_summary에 저장한다.
 * 의견이 2개 이상일 때만 만들고, 같은 문서는 최소 간격(trendizm.summary.min-interval, 기본 10분)을 두고 다시 만든다.
 */
public interface MemeSummaryService {

    /** 조건(의견 수·간격·API 키)이 맞으면 요약을 다시 만든다. 아니거나 실패하면 아무것도 바꾸지 않는다 */
    void refreshSummary(Long memeId);
}
