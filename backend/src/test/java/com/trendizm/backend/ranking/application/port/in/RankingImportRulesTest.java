package com.trendizm.backend.ranking.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RankingImportRulesTest {

    @Test
    @DisplayName("대소문자만 다른 키워드를 합칠 때 영문자가 4개 이하면 대문자, 5개 이상이면 소문자다 (전체 길이가 아니라 영문자 수)")
    void 합친_키워드_형태() {
        assertThat(RankingImportRules.caseMergedKeyword("l을가져가")).isEqualTo("L을가져가");
        assertThat(RankingImportRules.caseMergedKeyword("Tmi")).isEqualTo("TMI");
        assertThat(RankingImportRules.caseMergedKeyword("mbti테스트")).isEqualTo("MBTI테스트");
        assertThat(RankingImportRules.caseMergedKeyword("Storm챌린지")).isEqualTo("storm챌린지");
        assertThat(RankingImportRules.caseMergedKeyword("CopyPaste")).isEqualTo("copypaste");
    }
}
