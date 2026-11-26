package com.trendizm.backend.ranking.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator.Answer;
import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator.Post;
import com.trendizm.backend.ranking.application.port.out.RankingSummaryGenerator.Target;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RankingSummaryRulesTest {

    private static final Target TARGET = new Target(1L, "곶감챌린지", List.of(
            new Post("오늘도 연습해왔다고… #곶감챌린지", null),
            new Post(null, "앞 글이 끝나고")));

    private static Optional<String> reject(String summary, String evidence) {
        return RankingSummaryRules.rejectReason(TARGET, new Answer(1L, summary, evidence));
    }

    @Test
    @DisplayName("근거가 게시물 글에 있으면 저장한다. 공백 차이는 무시한다")
    void 저장() {
        assertThat(reject("요약이다.", "오늘도  연습해왔다고…")).isEmpty();
        assertThat(reject("요약이다.", "앞 글이\n끝나고")).isEmpty();
    }

    @Test
    @DisplayName("근거가 없거나, 글에 없거나, 두 글에 걸치거나, 키워드만 인용하면 저장하지 않는다")
    void 근거_없음() {
        for (String evidence : new String[] {null, "", "글에 없는 말", "#곶감챌린지앞 글이", "#곶감챌린지", "곶감챌린지"}) {
            assertThat(reject("요약이다.", evidence)).contains("요약의 근거를 게시물 글에서 찾지 못했습니다.");
        }
    }

    @Test
    @DisplayName("비어 있거나 너무 길거나 링크·계정 이름이 있으면 저장하지 않는다")
    void 쓸_수_없는_요약() {
        assertThat(reject(" ", "")).contains("게시물 글만으로는 무엇인지 알 수 없습니다.");
        assertThat(reject(null, "오늘도 연습")).contains("게시물 글만으로는 무엇인지 알 수 없습니다.");
        assertThat(reject("가".repeat(501), "오늘도 연습")).contains("요약이 500자를 넘습니다.");
        assertThat(reject("https://spam.example 참고", "오늘도 연습")).contains("요약에 링크나 계정 이름이 있습니다.");
        assertThat(reject("@someone 계정의 챌린지다.", "오늘도 연습")).contains("요약에 링크나 계정 이름이 있습니다.");
    }
}
