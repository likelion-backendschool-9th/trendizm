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

    /** 공개 문서 4개(저장 순서대로 TMI, 알잘딱깔센, 중꺾마, 알빠노), 숨김 문서 1개. 테스트가 끝나면 롤백된다 */
    @BeforeEach
    void setUp() {
        memeRepository.save(Meme.create("TMI", 1L, null, null, null));
        memeRepository.save(Meme.create("알잘딱깔센", 1L, "https://video.example.com/watch/01", "영상 제목", null));
        memeRepository.save(Meme.create("중꺾마", 1L, null, null, null));
        memeRepository.save(Meme.create("알빠노", 2L, null, null, null));
        Meme hidden = Meme.create("알숨김", 2L, null, null, null);
        hidden.hide();
        memeRepository.save(hidden);
    }

    @Test
    @DisplayName("단어로 공개 문서를 찾는다 (앞뒤 공백은 무시)")
    void 단어로_조회() {
        MemeResponse meme = memeService.getMeme(" 알잘딱깔센 ");

        assertThat(meme.word()).isEqualTo("알잘딱깔센");
        assertThat(meme.sourceUrl()).isEqualTo("https://video.example.com/watch/01");
        assertThat(meme.title()).isEqualTo("영상 제목");
        assertThat(meme.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("단어는 소문자로 저장하고, 대소문자와 관계없이 찾는다")
    void 대소문자_무시() {
        assertThat(memeService.getMeme("Tmi").word()).isEqualTo("tmi");
        assertThat(memeService.getMeme("tmi").word()).isEqualTo("tmi");
    }

    @Test
    @DisplayName("띄어쓰기가 다르면 다른 단어다")
    void 띄어쓰기_구분() {
        assertThatThrownBy(() -> memeService.getMeme("알잘딱 깔센"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(MemeErrorCode.MEME_NOT_FOUND);
    }

    @Test
    @DisplayName("없는 단어는 MEME_NOT_FOUND")
    void 없는_단어() {
        assertThatThrownBy(() -> memeService.getMeme("없는단어"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(MemeErrorCode.MEME_NOT_FOUND);
    }

    @Test
    @DisplayName("숨김 문서는 단어로 조회해도 MEME_NOT_FOUND")
    void 숨김_문서_조회() {
        assertThatThrownBy(() -> memeService.getMeme("알숨김"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(MemeErrorCode.MEME_NOT_FOUND);
    }

    @Test
    @DisplayName("목록은 공개 문서만 최근 등록순으로 보여 준다")
    void 최근_목록() {
        PageResponse<MemeResponse> page = memeService.getMemes(null, 0, 20);

        assertThat(page.content()).extracting(MemeResponse::word)
                .containsExactly("알빠노", "중꺾마", "알잘딱깔센", "tmi");
        assertThat(page.totalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("keyword를 주면 단어에 keyword가 들어간 공개 문서만")
    void 검색() {
        PageResponse<MemeResponse> page = memeService.getMemes(" 알 ", 0, 20);

        assertThat(page.content()).extracting(MemeResponse::word)
                .containsExactly("알빠노", "알잘딱깔센");
    }

    @Test
    @DisplayName("검색도 대소문자를 무시한다")
    void 검색_대소문자_무시() {
        PageResponse<MemeResponse> page = memeService.getMemes("TM", 0, 20);

        assertThat(page.content()).extracting(MemeResponse::word).containsExactly("tmi");
    }

    @Test
    @DisplayName("페이지 크기만큼 나눠서 보여 준다")
    void 페이지() {
        PageResponse<MemeResponse> page = memeService.getMemes("", 1, 3);

        assertThat(page.content()).extracting(MemeResponse::word).containsExactly("tmi");
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.hasNext()).isFalse();
    }
}
