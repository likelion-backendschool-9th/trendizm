package com.trendizm.backend.ranking.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlatformTest {

    @Test
    @DisplayName("slug로 플랫폼을 찾고, 없는 slug면 빈 값이다")
    void slug로_찾기() {
        assertThat(Platform.fromSlug("youtube")).contains(Platform.youtube);
        assertThat(Platform.fromSlug("x")).contains(Platform.x);
        assertThat(Platform.fromSlug("YOUTUBE")).isEmpty();
        assertThat(Platform.fromSlug("naver")).isEmpty();
        assertThat(Platform.fromSlug(null)).isEmpty();
    }

    @Test
    @DisplayName("게시물 ID로 플랫폼별 주소를 만들고, threads·tiktok은 작성자도 넣는다")
    void 게시물_주소() {
        assertThat(Platform.youtube.postUrl("dQw4w9WgXcQ", null)).isEqualTo("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        assertThat(Platform.instagram.postUrl("Da0E9_oBZM-", "무시됨")).isEqualTo("https://www.instagram.com/p/Da0E9_oBZM-/");
        assertThat(Platform.x.postUrl("1861234567890", null)).isEqualTo("https://x.com/i/status/1861234567890");
        assertThat(Platform.threads.postUrl("DAbc_12-x", "user.name")).isEqualTo("https://www.threads.com/@user.name/post/DAbc_12-x");
        assertThat(Platform.tiktok.postUrl("7312345678901234567", "dancer_kim"))
                .isEqualTo("https://www.tiktok.com/@dancer_kim/video/7312345678901234567");
        assertThat(Platform.threads.requiresAuthor()).isTrue();
        assertThat(Platform.instagram.requiresAuthor()).isFalse();
    }

    @Test
    @DisplayName("게시물 ID나 작성자에 허용하지 않는 문자가 있으면 주소를 만들지 않는다")
    void 잘못된_게시물_ID() {
        assertThatThrownBy(() -> Platform.instagram.postUrl("abc/../x", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Platform.youtube.postUrl("abc?x=1", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Platform.tiktok.postUrl("731", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Platform.tiktok.postUrl("731", "a/b")).isInstanceOf(IllegalArgumentException.class);
    }
}
