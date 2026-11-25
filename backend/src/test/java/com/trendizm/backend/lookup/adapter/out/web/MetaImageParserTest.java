package com.trendizm.backend.lookup.adapter.out.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MetaImageParserTest {

    private static final URI PAGE = URI.create("https://example.com/posts/1");

    @Test
    @DisplayName("og:image를 찾는다. 속성 순서·따옴표 종류·대소문자와 관계없다")
    void og_image() {
        assertThat(MetaImageParser.find("<head><meta property=\"og:image\" content=\"https://cdn.example.com/a.jpg\"></head>", PAGE))
                .contains("https://cdn.example.com/a.jpg");
        assertThat(MetaImageParser.find("<META CONTENT='https://cdn.example.com/b.jpg' PROPERTY='og:image' />", PAGE))
                .contains("https://cdn.example.com/b.jpg");
    }

    @Test
    @DisplayName("우선순위는 og:image:secure_url → og:image → twitter:image다")
    void 우선순위() {
        String html = """
                <meta name="twitter:image" content="https://cdn.example.com/twitter.jpg">
                <meta property="og:image" content="https://cdn.example.com/og.jpg">
                <meta property="og:image:secure_url" content="https://cdn.example.com/secure.jpg">
                """;
        assertThat(MetaImageParser.find(html, PAGE)).contains("https://cdn.example.com/secure.jpg");
        assertThat(MetaImageParser.find("<meta name=\"twitter:image\" content=\"https://cdn.example.com/t.jpg\">", PAGE))
                .contains("https://cdn.example.com/t.jpg");
    }

    @Test
    @DisplayName("주소 안의 &amp;amp; 같은 엔티티를 풀고, 상대 주소는 페이지 주소 기준으로 푼다")
    void 엔티티와_상대_주소() {
        assertThat(MetaImageParser.find("<meta property=\"og:image\" content=\"https://cdn.example.com/a.jpg?w=1&amp;h=2\">", PAGE))
                .contains("https://cdn.example.com/a.jpg?w=1&h=2");
        assertThat(MetaImageParser.find("<meta property=\"og:image\" content=\"/img/a.png\">", PAGE))
                .contains("https://example.com/img/a.png");
    }

    @Test
    @DisplayName("이미지 태그가 없거나, 비어 있거나, http(s)가 아닌 주소면 empty")
    void 없음() {
        assertThat(MetaImageParser.find("<html><title>제목</title></html>", PAGE)).isEmpty();
        assertThat(MetaImageParser.find("<meta property=\"og:image\" content=\" \">", PAGE)).isEmpty();
        assertThat(MetaImageParser.find("<meta property=\"og:image\" content=\"javascript:alert(1)\">", PAGE)).isEmpty();
        assertThat(MetaImageParser.find("<meta property=\"og:image\" content=\"data:image/png;base64,AAAA\">", PAGE)).isEmpty();
    }
}
