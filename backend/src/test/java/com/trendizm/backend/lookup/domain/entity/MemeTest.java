package com.trendizm.backend.lookup.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.lookup.domain.enums.MemeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemeTest {

    private static final String SOURCE = "https://youtu.be/abc123";

    @Test
    @DisplayName("새 문서는 공개 상태이고 단어 앞뒤 공백을 지우고, 출처 주소는 정규화해 저장한다")
    void 문서_생성() {
        Meme meme = Meme.create("  알잘딱깔센 ", 1L, SOURCE, "영상 제목", null);

        assertThat(meme.getWord()).isEqualTo("알잘딱깔센");
        assertThat(meme.getSourceUrl()).isEqualTo("https://youtube.com/watch?v=abc123");
        assertThat(meme.getStatus()).isEqualTo(MemeStatus.PUBLIC);
        assertThat(meme.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("출처 주소는 필수다")
    void 출처_필수() {
        assertThatThrownBy(() -> Meme.create("TMI", 1L, null, null, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("단어는 소문자로 저장하고 띄어쓰기는 그대로 둔다")
    void 단어_정규화() {
        Meme meme = Meme.create(" TMI ", 1L, SOURCE, null, null);

        assertThat(meme.getWord()).isEqualTo("tmi");
        assertThat(Meme.normalizeWord(" 알잘딱 깔센 ")).isEqualTo("알잘딱 깔센");
    }

    @Test
    @DisplayName("숨김과 해제")
    void 숨김_해제() {
        Meme meme = Meme.create("중꺾마", 1L, SOURCE, null, null);

        meme.hide();
        assertThat(meme.isHidden()).isTrue();

        meme.show();
        assertThat(meme.isHidden()).isFalse();
    }
}
