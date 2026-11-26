package com.trendizm.backend.ranking.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrendRankingLinkTest {

    private static TrendRankingLink link(String title) {
        return TrendRankingLink.create(1L, title, "https://www.instagram.com/p/abc/", null, null, null, 0);
    }

    @Test
    @DisplayName("제목이 없거나 공백뿐이면 null로 저장하고, 앞뒤 공백은 지운다")
    void 빈_제목() {
        assertThat(link(null).getTitle()).isNull();
        assertThat(link("  ").getTitle()).isNull();
        assertThat(link("  곶감 챌린지  ").getTitle()).isEqualTo("곶감 챌린지");
    }

    @Test
    @DisplayName("제목의 제어 문자는 지운다. 줄바꿈과 탭은 그대로 두고, 길이는 지운 뒤에 센다")
    void 제어_문자() {
        assertThat(link("곶감\u0000\u001B \u0085챌린지").getTitle()).isEqualTo("곶감 챌린지");
        assertThat(link("첫 줄\r\n둘째\t줄").getTitle()).isEqualTo("첫 줄\r\n둘째\t줄");
        assertThat(link("\u0000 \u0007").getTitle()).isNull();
        assertThat(link("\u0000 곶감 \u0000").getTitle()).isEqualTo("곶감");
        assertThat(link("\u0000" + "가".repeat(255)).getTitle()).isEqualTo("가".repeat(255));
    }

    @Test
    @DisplayName("제목이 255자를 넘으면 앞 255자만 남기고, 이모지(자바 문자 2개)가 중간에서 잘리지 않는다")
    void 긴_제목() {
        assertThat(link("가".repeat(300)).getTitle()).isEqualTo("가".repeat(255));

        String withEmoji = "가".repeat(254) + "😀" + "나".repeat(10);
        String title = link(withEmoji).getTitle();

        assertThat(title.codePointCount(0, title.length())).isEqualTo(255);
        assertThat(title).endsWith("😀");
    }
}
