package com.trendizm.backend.lookup.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.MemeResponse;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeService;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.Meme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemeServiceTest {

    @Autowired
    private MemeService memeService;

    @Autowired
    private MemeRepository memeRepository;

    private Meme tmiVideo;
    private Meme tmiPost;
    private Meme hidden;

    /**
     * 저장 순서대로 TMI(영상), 알잘딱깔센, 중꺾마, 알빠노, TMI(게시물) 공개 문서 5개와 숨김 문서 TMI(숨김 출처) 1개.
     * TMI는 출처가 달라 문서가 셋이다. 테스트가 끝나면 롤백된다
     */
    @BeforeEach
    void setUp() {
        tmiVideo = memeRepository.save(Meme.create("TMI", 1L, "https://youtu.be/tmi01", "영상 제목"));
        memeRepository.save(Meme.create("알잘딱깔센", 1L, "https://example.com/1", null));
        memeRepository.save(Meme.create("중꺾마", 1L, "https://example.com/2", null));
        memeRepository.save(Meme.create("알빠노", 2L, "https://example.com/3", null));
        tmiPost = memeRepository.save(Meme.create("tmi", 2L, "https://x.com/someone/status/1", null));
        hidden = Meme.create("TMI", 2L, "https://example.com/hidden", null);
        hidden.hide();
        memeRepository.save(hidden);
    }

    @Test
    @DisplayName("단어로 찾으면 출처별 공개 문서 목록을 최근 등록순으로 준다 (대소문자·앞뒤 공백 무시, 숨김 제외)")
    void 단어로_목록() {
        PageResponse<MemeResponse> page = memeService.getMemesByWord(" Tmi ", null, 0, 20);

        assertThat(page.content()).extracting(MemeResponse::id).containsExactly(tmiPost.getId(), tmiVideo.getId());
        assertThat(page.content()).extracting(MemeResponse::sourceUrl)
                .containsExactly("https://x.com/i/status/1", "https://youtube.com/watch?v=tmi01");
    }

    @Test
    @DisplayName("띄어쓰기가 다르거나 없는 단어면 빈 목록")
    void 단어로_목록_없음() {
        assertThat(memeService.getMemesByWord("알잘딱 깔센", null, 0, 20).content()).isEmpty();
        assertThat(memeService.getMemesByWord("없는단어", null, 0, 20).content()).isEmpty();
    }

    @Test
    @DisplayName("출처 주소를 함께 주면 그 출처의 문서만 준다 (모바일 링크로 찾아도 같은 문서)")
    void 단어와_출처로_찾기() {
        PageResponse<MemeResponse> page = memeService.getMemesByWord("TMI", "https://m.youtube.com/watch?v=tmi01&t=3s", 0, 20);

        assertThat(page.content()).extracting(MemeResponse::id).containsExactly(tmiVideo.getId());
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("출처가 다르거나 숨김 문서면 빈 목록이고, 빈 출처 주소는 출처로 거르지 않는다")
    void 단어와_출처로_찾기_없음() {
        assertThat(memeService.getMemesByWord("TMI", "https://example.com/other", 0, 20).content()).isEmpty();
        assertThat(memeService.getMemesByWord("TMI", "https://example.com/hidden", 0, 20).content()).isEmpty();
        assertThat(memeService.getMemesByWord("TMI", " ", 0, 20).totalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("문서 id로 하나를 조회한다")
    void 문서_조회() {
        MemeResponse meme = memeService.getMeme(tmiVideo.getId());

        assertThat(meme.word()).isEqualTo("tmi");
        assertThat(meme.title()).isEqualTo("영상 제목");
        assertThat(meme.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("없거나 숨김인 문서는 MEME_NOT_FOUND")
    void 없는_문서() {
        for (Long id : new Long[] {-1L, hidden.getId()}) {
            assertThatThrownBy(() -> memeService.getMeme(id))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(MemeErrorCode.MEME_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("목록은 공개 문서만 최근 등록순으로 보여 준다 (같은 단어도 출처별로 나온다)")
    void 최근_목록() {
        PageResponse<MemeResponse> page = memeService.getMemes(null, 0, 20);

        assertThat(page.content()).extracting(MemeResponse::word)
                .containsExactly("tmi", "알빠노", "중꺾마", "알잘딱깔센", "tmi");
        assertThat(page.totalElements()).isEqualTo(5);
    }

    @Test
    @DisplayName("keyword를 주면 단어에 keyword가 들어간 공개 문서만")
    void 검색() {
        PageResponse<MemeResponse> page = memeService.getMemes(" 알 ", 0, 20);

        assertThat(page.content()).extracting(MemeResponse::word).containsExactly("알빠노", "알잘딱깔센");
    }

    @Test
    @DisplayName("검색도 대소문자를 무시한다")
    void 검색_대소문자_무시() {
        PageResponse<MemeResponse> page = memeService.getMemes("TM", 0, 20);

        assertThat(page.content()).extracting(MemeResponse::id).containsExactly(tmiPost.getId(), tmiVideo.getId());
    }

    @Test
    @DisplayName("페이지 크기만큼 나눠서 보여 준다")
    void 페이지() {
        PageResponse<MemeResponse> page = memeService.getMemes("", 1, 3);

        assertThat(page.content()).extracting(MemeResponse::word).containsExactly("알잘딱깔센", "tmi");
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.hasNext()).isFalse();
    }
}
