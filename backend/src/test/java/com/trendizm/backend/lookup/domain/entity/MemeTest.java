package com.trendizm.backend.lookup.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.lookup.domain.enums.MemeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemeTest {

    @Test
    @DisplayName("새 문서는 공개 상태이고 단어 앞뒤 공백을 지운다")
    void 문서_생성() {
        Meme meme = Meme.create("  알잘딱깔센 ", 1L, null, null, null);

        assertThat(meme.getWord()).isEqualTo("알잘딱깔센");
        assertThat(meme.getStatus()).isEqualTo(MemeStatus.PUBLIC);
        assertThat(meme.getUserId()).isEqualTo(1L);
        assertThat(meme.getSourceUrl()).isNull();
    }

    @Test
    @DisplayName("단어는 소문자로 저장하고 띄어쓰기는 그대로 둔다")
    void 단어_정규화() {
        Meme meme = Meme.create(" TMI ", 1L, null, null, null);

        assertThat(meme.getWord()).isEqualTo("tmi");
        assertThat(Meme.normalizeWord(" 알잘딱 깔센 ")).isEqualTo("알잘딱 깔센");
    }

    @Test
    @DisplayName("숨김과 해제")
    void 숨김_해제() {
        Meme meme = Meme.create("중꺾마", 1L, null, null, null);

        meme.hide();
        assertThat(meme.isHidden()).isTrue();

        meme.show();
        assertThat(meme.isHidden()).isFalse();
    }
}
