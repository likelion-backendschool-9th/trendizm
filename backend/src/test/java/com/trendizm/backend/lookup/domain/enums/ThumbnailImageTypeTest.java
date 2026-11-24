package com.trendizm.backend.lookup.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThumbnailImageTypeTest {

    static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10};
    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
    static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P'};

    @Test
    @DisplayName("Content-Type은 대소문자·charset과 관계없이 찾고, SVG·HTML 같은 형식은 받지 않는다")
    void Content_Type() {
        assertThat(ThumbnailImageType.fromContentType("image/jpeg")).contains(ThumbnailImageType.JPG);
        assertThat(ThumbnailImageType.fromContentType("Image/PNG; charset=binary")).contains(ThumbnailImageType.PNG);
        assertThat(ThumbnailImageType.fromContentType("image/svg+xml")).isEmpty();
        assertThat(ThumbnailImageType.fromContentType("text/html")).isEmpty();
        assertThat(ThumbnailImageType.fromContentType(null)).isEmpty();
    }

    @Test
    @DisplayName("파일 앞부분이 형식과 맞아야 한다. 이미지라고 보낸 HTML은 받지 않는다")
    void 매직_넘버() {
        assertThat(ThumbnailImageType.JPG.matches(JPG)).isTrue();
        assertThat(ThumbnailImageType.PNG.matches(PNG)).isTrue();
        assertThat(ThumbnailImageType.WEBP.matches(WEBP)).isTrue();
        assertThat(ThumbnailImageType.GIF.matches("GIF89a".getBytes(StandardCharsets.US_ASCII))).isTrue();

        assertThat(ThumbnailImageType.PNG.matches(JPG)).isFalse();
        assertThat(ThumbnailImageType.JPG.matches("<html><script>".getBytes(StandardCharsets.UTF_8))).isFalse();
        assertThat(ThumbnailImageType.WEBP.matches(new byte[] {'R', 'I', 'F', 'F'})).isFalse();
        assertThat(ThumbnailImageType.JPG.matches(new byte[0])).isFalse();
    }
}
