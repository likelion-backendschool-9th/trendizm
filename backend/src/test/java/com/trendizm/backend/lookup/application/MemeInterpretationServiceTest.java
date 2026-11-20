package com.trendizm.backend.lookup.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;
import com.trendizm.backend.lookup.application.dto.MemeResponse;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeInterpretationService;
import com.trendizm.backend.lookup.application.port.MemeService;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemeInterpretationServiceTest {

    @Autowired
    private MemeInterpretationService interpretationService;

    @Autowired
    private MemeService memeService;

    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private MemeInterpretationRepository interpretationRepository;

    private Meme tmi;
    private Meme hidden;

    /** 공개 문서 tmi(의견 없음), 숨김 문서 알숨김. 테스트가 끝나면 롤백된다 */
    @BeforeEach
    void setUp() {
        tmi = memeRepository.save(Meme.create("TMI", 1L, null, null, null));
        hidden = Meme.create("알숨김", 1L, null, null, null);
        hidden.hide();
        memeRepository.save(hidden);
    }

    private static CreateMemeInterpretationCommand command(String word) {
        return new CreateMemeInterpretationCommand(word, 2L, "뜻", "내용", "https://video.example.com/watch/01", "영상 제목", null);
    }

    @Test
    @DisplayName("문서가 없는 단어에 첫 의견을 쓰면 문서가 함께 만들어지고, 출처는 문서에 저장된다")
    void 첫_의견으로_문서_생성() {
        MemeInterpretationResponse created = interpretationService.createInterpretation(command(" 알잘딱깔센 "));

        MemeResponse meme = memeService.getMeme("알잘딱깔센");
        assertThat(created.memeId()).isEqualTo(meme.id());
        assertThat(created.userId()).isEqualTo(2L);
        assertThat(created.agreeCount()).isZero();
        assertThat(created.disagreeCount()).isZero();
        assertThat(meme.sourceUrl()).isEqualTo("https://video.example.com/watch/01");
        assertThat(meme.title()).isEqualTo("영상 제목");
    }

    @Test
    @DisplayName("대소문자만 다른 단어는 있는 문서에 의견을 붙이고, 출처 값은 무시한다")
    void 대소문자만_다르면_기존_문서() {
        MemeInterpretationResponse created = interpretationService.createInterpretation(command("Tmi"));

        assertThat(created.memeId()).isEqualTo(tmi.getId());
        assertThat(memeService.getMeme("tmi").sourceUrl()).isNull();
        assertThat(memeService.getMemes("tmi", 0, 20).totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("숨김 문서의 단어로 쓰면 새 문서를 만들지 않고 MEME_NOT_FOUND")
    void 숨김_문서에_작성() {
        assertThatThrownBy(() -> interpretationService.createInterpretation(command("알숨김")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(MemeErrorCode.MEME_NOT_FOUND);
    }

    @Test
    @DisplayName("목록은 찬성 - 반대가 큰 순서, 같으면 최신순")
    void 신뢰도순_목록() {
        MemeInterpretation low = saveWithVotes("반대 많음", 1, 3);   // -2
        MemeInterpretation older = saveWithVotes("먼저 쓴 1점", 2, 1); // 1
        MemeInterpretation newer = saveWithVotes("나중 쓴 1점", 1, 0); // 1
        MemeInterpretation top = saveWithVotes("찬성 많음", 5, 0);   // 5

        PageResponse<MemeInterpretationResponse> page = interpretationService.getInterpretations(tmi.getId(), 0, 20);

        assertThat(page.content()).extracting(MemeInterpretationResponse::id)
                .containsExactly(top.getId(), newer.getId(), older.getId(), low.getId());
    }

    @Test
    @DisplayName("목록도 페이지 크기만큼 나눠서 보여 준다")
    void 목록_페이지() {
        for (int i = 0; i < 3; i++) {
            saveWithVotes("뜻" + i, 0, 0);
        }

        PageResponse<MemeInterpretationResponse> page = interpretationService.getInterpretations(tmi.getId(), 1, 2);

        assertThat(page.content()).hasSize(1);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    @DisplayName("없거나 숨김인 문서의 의견 목록은 MEME_NOT_FOUND")
    void 없는_문서_목록() {
        for (Long memeId : new Long[] {-1L, hidden.getId()}) {
            assertThatThrownBy(() -> interpretationService.getInterpretations(memeId, 0, 20))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(MemeErrorCode.MEME_NOT_FOUND);
        }
    }

    /** 투표 기능 전이라 찬성·반대 수는 필드에 직접 넣는다 */
    private MemeInterpretation saveWithVotes(String meaning, int agree, int disagree) {
        MemeInterpretation interpretation = MemeInterpretation.create(tmi.getId(), 1L, meaning, "내용");
        ReflectionTestUtils.setField(interpretation, "agreeCount", agree);
        ReflectionTestUtils.setField(interpretation, "disagreeCount", disagree);
        return interpretationRepository.save(interpretation);
    }
}
