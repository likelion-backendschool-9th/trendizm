package com.trendizm.backend.lookup.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SourceUrlTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
            // 유튜브: 모바일·단축·쇼츠·공유 링크를 watch?v=ID 하나로
            "https://www.youtube.com/watch?v=abc123, https://youtube.com/watch?v=abc123",
            "https://m.youtube.com/watch?v=abc123&t=30s, https://youtube.com/watch?v=abc123",
            "https://youtu.be/abc123?si=XYZ, https://youtube.com/watch?v=abc123",
            "http://youtube.com/shorts/abc123?feature=share, https://youtube.com/watch?v=abc123",
            // X: 트위터 주소·모바일·계정 이름과 관계없이 글 id로
            "https://twitter.com/someone/status/1234567890?s=20, https://x.com/i/status/1234567890",
            "https://mobile.twitter.com/someone/status/1234567890, https://x.com/i/status/1234567890",
            "https://x.com/renamed/status/1234567890/photo/1, https://x.com/i/status/1234567890",
            // 인스타그램: 게시물·릴스를 /p/코드로
            "https://www.instagram.com/p/Cxyz_1/?igsh=abc, https://instagram.com/p/Cxyz_1",
            "https://instagram.com/reel/Cxyz_1/?utm_source=ig_web_copy_link, https://instagram.com/p/Cxyz_1",
            // 스레드: threads.net·threads.com을 하나로
            "https://www.threads.net/@Someone/post/C9abc?xmt=AQ, https://threads.com/@someone/post/C9abc",
            "https://threads.com/@someone/post/C9abc, https://threads.com/@someone/post/C9abc",
            // 틱톡
            "https://www.tiktok.com/@Someone/video/7212345678901234567?is_from_webapp=1, https://tiktok.com/@someone/video/7212345678901234567",
            "https://m.tiktok.com/@someone/video/7212345678901234567, https://tiktok.com/@someone/video/7212345678901234567",
            // 그 밖의 주소: https, www 제거, #·추적 파라미터·끝 / 제거, 나머지 파라미터는 유지
            "HTTP://WWW.Example.com/post/1/?utm_source=x&page=2#comment, https://example.com/post/1?page=2",
            "https://blog.example.com/search?s=밈, https://blog.example.com/search?s=밈",
    })
    void 정규화(String raw, String expected) {
        assertThat(SourceUrl.normalize(raw)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0}은 그대로")
    @CsvSource({
            // 단축 링크는 접속해 봐야 원래 주소를 알 수 있어서 바꾸지 않는다(https 통일 등 일반 규칙만)
            "https://vm.tiktok.com/ZMabc123/, https://vm.tiktok.com/ZMabc123",
            // 게시물이 아닌 플랫폼 주소는 일반 규칙만
            "https://www.youtube.com/@channel, https://youtube.com/@channel",
    })
    void 플랫폼_규칙에_안_맞는_주소(String raw, String expected) {
        assertThat(SourceUrl.normalize(raw)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0}의 썸네일은 {1}")
    @CsvSource({
            "https://youtu.be/dQw4w9WgXcQ?si=abc, https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
            "https://m.youtube.com/watch?v=a_b-1&t=30s, https://i.ytimg.com/vi/a_b-1/hqdefault.jpg",
            "https://www.youtube.com/shorts/xyz789, https://i.ytimg.com/vi/xyz789/hqdefault.jpg",
    })
    void 유튜브_썸네일(String raw, String expected) {
        assertThat(SourceUrl.youtubeThumbnail(SourceUrl.normalize(raw))).contains(expected);
    }

    @ParameterizedTest(name = "{0}은 썸네일 없음")
    @ValueSource(strings = {
            "https://x.com/someone/status/123",
            "https://www.instagram.com/p/Cabc/",
            "https://www.tiktok.com/@user/video/123",
            "https://www.youtube.com/@channel",
            "https://example.com/watch?v=abc",
            // 영상 ID에 영문·숫자·_·- 외의 글자가 있으면 만들지 않는다
            "https://youtube.com/watch?v=abc%22onerror",
    })
    void 유튜브가_아니면_썸네일_없음(String raw) {
        assertThat(SourceUrl.youtubeThumbnail(SourceUrl.normalize(raw))).isEmpty();
    }
}
