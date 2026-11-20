package com.trendizm.backend.rating.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.rating.domain.enums.Sentiment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RatingTest {

    @Test
    @DisplayName("후기를 수정하면 호/불호·별점·제목·본문·시청 정도가 바뀐다")
    void 수정() {
        Rating rating = Rating.create(1L, 2L, Sentiment.LIKE, 5, "최고", "재밌어요", 3);

        rating.update(Sentiment.DISLIKE, 2, "별로", "뒤로 갈수록 아쉬워요", null);

        assertThat(rating.getSentiment()).isEqualTo(Sentiment.DISLIKE);
        assertThat(rating.getScore()).isEqualTo(2);
        assertThat(rating.getTitle()).isEqualTo("별로");
        assertThat(rating.getBody()).isEqualTo("뒤로 갈수록 아쉬워요");
        assertThat(rating.getProgress()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1})
    @DisplayName("별점이 1~5가 아니면 만들 수도 고칠 수도 없다")
    void 잘못된_별점(int score) {
        assertThatThrownBy(() -> Rating.create(1L, 2L, Sentiment.LIKE, score, "제목", "본문", null))
                .isInstanceOf(IllegalArgumentException.class);

        Rating rating = Rating.create(1L, 2L, Sentiment.LIKE, 3, "제목", "본문", null);
        assertThatThrownBy(() -> rating.update(Sentiment.LIKE, score, "제목", "본문", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(rating.getScore()).as("실패하면 바뀌지 않는다").isEqualTo(3);
    }

    @Test
    @DisplayName("시청 정도가 음수면 IllegalArgumentException")
    void 잘못된_시청_정도() {
        assertThatThrownBy(() -> Rating.create(1L, 2L, Sentiment.LIKE, 3, "제목", "본문", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
