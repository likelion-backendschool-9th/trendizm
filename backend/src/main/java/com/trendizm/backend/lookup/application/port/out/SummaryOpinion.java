package com.trendizm.backend.lookup.application.port.out;

/** 요약에 넣는 의견 하나. 신뢰도순으로 넘긴다 */
public record SummaryOpinion(String meaning, String content, int agreeCount, int disagreeCount) {
}
