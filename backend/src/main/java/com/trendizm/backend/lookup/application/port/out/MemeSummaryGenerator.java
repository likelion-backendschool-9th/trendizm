package com.trendizm.backend.lookup.application.port.out;

import java.util.List;
import java.util.Optional;

/**
 * 의견들을 AI로 요약한다. 구현체는 adapter/out/ai/GeminiSummaryGenerator.
 * 실패(한도 초과 429, 시간 초과, 차단된 응답 등)하면 예외 대신 empty를 준다. 요약은 없어도 문서는 정상이다.
 */
public interface MemeSummaryGenerator {

    /** API 키가 설정되어 있으면 true. 키가 없으면(로컬·테스트) 요약 기능만 꺼진다 */
    boolean isConfigured();

    /** 지금 요약을 만들 수 있으면 true. 키가 있어도 한도 초과(429)로 잠시 멈춘 동안은 false */
    boolean isEnabled();

    /** 단어와 신뢰도순 의견으로 요약을 만든다 */
    Optional<String> summarize(String word, List<SummaryOpinion> opinions);
}
