package com.trendizm.backend.lookup.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.accuse.application.dto.AccuseResponse;
import com.trendizm.backend.accuse.application.exception.AccuseErrorCode;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.lookup.application.dto.CreateMemeAccuseCommand;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeAccuseService;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.Meme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemeAccuseServiceTest {

    private static final Long USER = 7L;

    @Autowired
    private MemeAccuseService memeAccuseService;

    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Meme tmi;
    private Meme hidden;

    /** 공개 문서 tmi와 숨김 문서 알숨김. 테스트가 끝나면 롤백된다 */
    @BeforeEach
    void setUp() {
        tmi = memeRepository.save(Meme.create("TMI", 1L, "https://youtu.be/tmi01", null));
        hidden = Meme.create("알숨김", 1L, "https://example.com/hidden", null);
        hidden.hide();
        memeRepository.save(hidden);
    }

    @Test
    @DisplayName("공개 문서를 신고하면 대상은 memes 테이블(Meme.TABLE_NAME), 대상 id는 문서 id로 저장된다")
    void 문서_신고() {
        AccuseResponse saved =
                memeAccuseService.accuseMeme(new CreateMemeAccuseCommand(tmi.getId(), USER, "hate", "비하 표현"));

        assertThat(Meme.TABLE_NAME).isEqualTo("memes");
        assertThat(saved.reason()).isEqualTo("hate");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM accuses WHERE target = 'memes' AND target_id = ? AND user_id = ?",
                Long.class, tmi.getId(), USER)).isEqualTo(1);
    }

    @Test
    @DisplayName("없거나 숨김인 문서는 MEME_NOT_FOUND이고 신고가 저장되지 않는다")
    void 없는_문서_신고() {
        for (Long memeId : new Long[] {-1L, hidden.getId()}) {
            assertThatThrownBy(() -> memeAccuseService.accuseMeme(new CreateMemeAccuseCommand(memeId, USER, "hate", null)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(MemeErrorCode.MEME_NOT_FOUND);
        }
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accuses", Long.class)).isZero();
    }

    @Test
    @DisplayName("문서를 처음 만든 회원은 자기 문서를 신고할 수 없다 (ACCUSE_OWN_TARGET)")
    void 자기_문서_신고() {
        Long owner = tmi.getUserId();

        assertThatThrownBy(() -> memeAccuseService.accuseMeme(new CreateMemeAccuseCommand(tmi.getId(), owner, "hate", null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AccuseErrorCode.ACCUSE_OWN_TARGET);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accuses", Long.class)).isZero();
    }

    @Test
    @DisplayName("같은 문서를 다시 신고하면 accuse의 ACCUSE_ALREADY_EXISTS가 그대로 나간다")
    void 문서_중복_신고() {
        memeAccuseService.accuseMeme(new CreateMemeAccuseCommand(tmi.getId(), USER, "hate", null));

        assertThatThrownBy(() -> memeAccuseService.accuseMeme(new CreateMemeAccuseCommand(tmi.getId(), USER, "ad", null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AccuseErrorCode.ACCUSE_ALREADY_EXISTS);
    }
}
