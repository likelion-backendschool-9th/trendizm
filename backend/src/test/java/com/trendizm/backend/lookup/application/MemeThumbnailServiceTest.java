package com.trendizm.backend.lookup.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.port.MemeInterpretationService;
import com.trendizm.backend.lookup.application.port.MemeThumbnailService;
import com.trendizm.backend.lookup.application.port.out.DownloadedImage;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.application.port.out.ThumbnailStorage;
import com.trendizm.backend.lookup.application.port.out.WebImageClient;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.enums.ThumbnailImageType;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import com.trendizm.backend.member.application.port.MemberService;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

/**
 * 썸네일 채우기. 외부 접속(WebImageClient)과 파일 저장(ThumbnailStorage)은 mock으로 바꾼다.
 * 썸네일은 문서 작성이 커밋된 뒤 다른 스레드·트랜잭션에서 채워서, 테스트 데이터도 커밋되어야 한다.
 * 그래서 @Transactional을 붙이지 않고, 테스트가 끝나면 테이블을 비운다.
 */
@SpringBootTest
@TestPropertySource(properties = "trendizm.thumbnail.fetch-enabled=true")
@Sql(statements = {"DELETE FROM meme_interpretations", "DELETE FROM memes", "DELETE FROM users"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MemeThumbnailServiceTest {

    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
    private static final String FILENAME = "0123456789abcdef0123456789abcdef.jpg";

    @Autowired
    private MemeThumbnailService thumbnailService;

    @Autowired
    private MemeInterpretationService interpretationService;

    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private MemberService memberService;

    @MockitoBean
    private WebImageClient webImageClient;

    @MockitoBean
    private ThumbnailStorage thumbnailStorage;

    private Long saveMeme(String sourceUrl) {
        return memeRepository.save(Meme.create("TMI", 1L, sourceUrl, null)).getId();
    }

    private String thumbnailOf(Long memeId) {
        return memeRepository.findById(memeId).orElseThrow().getThumbnail();
    }

    @Test
    @DisplayName("유튜브는 페이지에 접속하지 않고 영상 ID 규칙의 이미지를 받아 저장하고 파일명을 기록한다")
    void 유튜브() {
        Long memeId = saveMeme("https://youtu.be/abc123");
        given(webImageClient.download("https://i.ytimg.com/vi/abc123/hqdefault.jpg"))
                .willReturn(Optional.of(new DownloadedImage(JPG, ThumbnailImageType.JPG)));
        given(thumbnailStorage.save(JPG, ThumbnailImageType.JPG)).willReturn(FILENAME);

        thumbnailService.fillThumbnail(memeId);

        assertThat(thumbnailOf(memeId)).isEqualTo(FILENAME);
        verify(webImageClient, never()).findImageUrl(any());
    }

    @Test
    @DisplayName("다른 출처는 페이지 메타데이터(og:image)의 이미지를 받아 저장한다")
    void 메타데이터() {
        Long memeId = saveMeme("https://www.instagram.com/p/abc/");
        given(webImageClient.findImageUrl("https://instagram.com/p/abc")).willReturn(Optional.of("https://cdn.example.com/a.jpg"));
        given(webImageClient.download("https://cdn.example.com/a.jpg"))
                .willReturn(Optional.of(new DownloadedImage(JPG, ThumbnailImageType.JPG)));
        given(thumbnailStorage.save(JPG, ThumbnailImageType.JPG)).willReturn(FILENAME);

        thumbnailService.fillThumbnail(memeId);

        assertThat(thumbnailOf(memeId)).isEqualTo(FILENAME);
    }

    @Test
    @DisplayName("이미지를 못 찾거나 못 받으면 저장하지 않고 썸네일은 비워 둔다")
    void 이미지_없음() {
        Long noMeta = saveMeme("https://x.com/someone/status/1");
        Long failedDownload = saveMeme("https://example.com/page");
        given(webImageClient.findImageUrl("https://x.com/someone/status/1")).willReturn(Optional.empty());
        given(webImageClient.findImageUrl("https://example.com/page")).willReturn(Optional.of("https://cdn.example.com/a.svg"));
        given(webImageClient.download("https://cdn.example.com/a.svg")).willReturn(Optional.empty());

        thumbnailService.fillThumbnail(noMeta);
        thumbnailService.fillThumbnail(failedDownload);

        assertThat(thumbnailOf(noMeta)).isNull();
        assertThat(thumbnailOf(failedDownload)).isNull();
        verifyNoInteractions(thumbnailStorage);
    }

    @Test
    @DisplayName("저장 중 예외가 나도 밖으로 던지지 않고(비동기 실행) 썸네일은 비워 둔다. 이미 썸네일이 있거나 없는 문서면 아무것도 하지 않는다")
    void 실패와_건너뛰기() {
        Long memeId = saveMeme("https://youtu.be/abc123");
        given(webImageClient.download(any())).willReturn(Optional.of(new DownloadedImage(JPG, ThumbnailImageType.JPG)));
        given(thumbnailStorage.save(any(), any())).willThrow(new IllegalStateException("디스크 가득 참"));

        thumbnailService.fillThumbnail(memeId);
        assertThat(thumbnailOf(memeId)).isNull();

        Meme filled = Meme.create("TMI", 1L, "https://youtu.be/filled", null);
        filled.changeThumbnail(FILENAME);
        Long filledId = memeRepository.save(filled).getId();
        thumbnailService.fillThumbnail(filledId);
        thumbnailService.fillThumbnail(-1L);
        verify(webImageClient, never()).download("https://i.ytimg.com/vi/filled/hqdefault.jpg");
    }

    @Test
    @DisplayName("의견 작성으로 문서가 새로 만들어지면, 커밋 뒤 따로 썸네일을 채운다")
    void 문서_생성_후_자동() throws Exception {
        Long writer = memberService.register(new RegisterMemberCommand("thumb@test.com", "encoded", "썸네일러", false));
        given(webImageClient.download("https://i.ytimg.com/vi/new123/hqdefault.jpg"))
                .willReturn(Optional.of(new DownloadedImage(JPG, ThumbnailImageType.JPG)));
        given(thumbnailStorage.save(JPG, ThumbnailImageType.JPG)).willReturn(FILENAME);

        Long memeId = interpretationService.createInterpretation(new CreateMemeInterpretationCommand(
                "TMI", writer, "뜻", "내용", "https://youtu.be/new123", null)).memeId();

        Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        while (thumbnailOf(memeId) == null && Instant.now().isBefore(deadline)) {
            Thread.sleep(50);
        }
        assertThat(thumbnailOf(memeId)).isEqualTo(FILENAME);
    }

    @Test
    @DisplayName("저장한 이미지를 파일명으로 읽어 Content-Type과 함께 준다")
    void 이미지_읽기() {
        given(thumbnailStorage.load(FILENAME)).willReturn(Optional.of(new DownloadedImage(JPG, ThumbnailImageType.JPG)));

        assertThat(thumbnailService.getThumbnail(FILENAME)).hasValueSatisfying(image -> {
            assertThat(image.content()).isEqualTo(JPG);
            assertThat(image.contentType()).isEqualTo("image/jpeg");
        });
        assertThat(thumbnailService.getThumbnail("없는.jpg")).isEmpty();
    }
}
