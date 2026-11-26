package com.trendizm.backend.accuse.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.accuse.application.dto.AccuseResponse;
import com.trendizm.backend.accuse.application.dto.CreateAccuseCommand;
import com.trendizm.backend.accuse.application.exception.AccuseErrorCode;
import com.trendizm.backend.accuse.application.port.AccuseService;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.error.ErrorCode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AccuseServiceTest {

    private static final String TARGET = "memes";
    private static final Long MEME_ID = 10L;
    private static final Long USER = 1L;
    private static final Long OTHER_USER = 2L;
    /** 신고 대상의 작성자 */
    private static final Long OWNER = 99L;

    @Autowired
    private AccuseService accuseService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static CreateAccuseCommand command(Long userId, String reason, String description) {
        return new CreateAccuseCommand(TARGET, MEME_ID, OWNER, userId, reason, description);
    }

    @Test
    @DisplayName("신고를 저장하고 사유는 slug로 돌려준다. 대상 테이블·대상 id는 저장만 하고 응답에는 넣지 않는다. 처리 대기 중으로 시작한다")
    void 신고_저장() {
        AccuseResponse saved = accuseService.accuse(command(USER, "hate", "특정 지역을 비하하는 단어입니다."));

        assertThat(saved.id()).isNotNull();
        assertThat(saved.reason()).isEqualTo("hate");
        assertThat(saved.description()).isEqualTo("특정 지역을 비하하는 단어입니다.");
        assertThat(saved.createdAt()).isNotNull();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT target || ',' || target_id || ',' || reason || ',' || user_id FROM accuses WHERE id = ?",
                String.class, saved.id()))
                .isEqualTo("memes," + MEME_ID + ",HATE," + USER);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT closed_at IS NULL FROM accuses WHERE id = ?", Boolean.class, saved.id())).isTrue();
    }

    @Test
    @DisplayName("사유는 대소문자·앞뒤 공백을 무시하고, 기타가 아니면 내용 없이 신고할 수 있다")
    void 사유_slug() {
        assertThat(accuseService.accuse(command(USER, " SEXUAL ", null)).reason()).isEqualTo("sexual");
        assertThat(accuseService.accuse(new CreateAccuseCommand(TARGET, 11L, OWNER, USER, "Ad", "   ")).description())
                .isNull();
    }

    @Test
    @DisplayName("허위 정보(false_info) 사유로 신고할 수 있다")
    void 허위_정보() {
        assertThat(accuseService.accuse(command(USER, "false_info", null)).reason()).isEqualTo("false_info");
    }

    @Test
    @DisplayName("대상의 작성자가 자기 글을 신고하면 ACCUSE_OWN_TARGET이고 저장하지 않는다")
    void 자기_글_신고() {
        assertError(() -> accuseService.accuse(command(OWNER, "hate", null)), AccuseErrorCode.ACCUSE_OWN_TARGET);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accuses", Long.class)).isZero();
    }

    @Test
    @DisplayName("작성자를 알 수 없는 대상(탈퇴 등으로 null)은 누구나 신고할 수 있다")
    void 작성자_없는_대상() {
        assertThat(accuseService.accuse(new CreateAccuseCommand(TARGET, MEME_ID, null, USER, "hate", null)).id())
                .isNotNull();
    }

    @Test
    @DisplayName("없는 사유면 ACCUSE_INVALID_REASON")
    void 없는_사유() {
        assertError(() -> accuseService.accuse(command(USER, "spam", null)), AccuseErrorCode.ACCUSE_INVALID_REASON);
        assertError(() -> accuseService.accuse(command(USER, null, null)), AccuseErrorCode.ACCUSE_INVALID_REASON);
    }

    @Test
    @DisplayName("기타인데 내용이 없거나 공백뿐이면 ACCUSE_DESCRIPTION_REQUIRED")
    void 기타_내용_필수() {
        assertError(() -> accuseService.accuse(command(USER, "etc", null)),
                AccuseErrorCode.ACCUSE_DESCRIPTION_REQUIRED);
        assertError(() -> accuseService.accuse(command(USER, "etc", " \n ")),
                AccuseErrorCode.ACCUSE_DESCRIPTION_REQUIRED);

        assertThat(accuseService.accuse(command(USER, "etc", "도배 글입니다")).description()).isEqualTo("도배 글입니다");
    }

    @Test
    @DisplayName("내용은 앞뒤 공백을 지운 뒤 500자까지 받고, 넘으면 ACCUSE_DESCRIPTION_TOO_LONG")
    void 내용_길이() {
        assertThat(accuseService.accuse(command(USER, "etc", "  " + "가".repeat(500) + "  ")).description())
                .hasSize(500);
        assertError(() -> accuseService.accuse(command(OTHER_USER, "etc", "가".repeat(501))),
                AccuseErrorCode.ACCUSE_DESCRIPTION_TOO_LONG);
    }

    @Test
    @DisplayName("같은 사람이 같은 대상을 다시 신고하면 사유가 달라도 ACCUSE_ALREADY_EXISTS. 다른 사람·다른 대상·다른 테이블의 같은 id는 신고할 수 있다")
    void 중복_신고() {
        accuseService.accuse(command(USER, "hate", null));

        assertError(() -> accuseService.accuse(command(USER, "ad", null)), AccuseErrorCode.ACCUSE_ALREADY_EXISTS);
        accuseService.accuse(command(OTHER_USER, "hate", null));
        accuseService.accuse(new CreateAccuseCommand(TARGET, MEME_ID + 1, OWNER, USER, "hate", null));
        accuseService.accuse(new CreateAccuseCommand("ratings", MEME_ID, OWNER, USER, "hate", null));

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accuses", Long.class)).isEqualTo(4);
    }

    @Test
    @DisplayName("관리자가 처리한 신고가 있어도 같은 사람은 다시 신고할 수 없다")
    void 처리된_뒤_재신고() {
        Long id = accuseService.accuse(command(USER, "hate", null)).id();
        jdbcTemplate.update("UPDATE accuses SET closed_at = CURRENT_TIMESTAMP WHERE id = ?", id);

        assertError(() -> accuseService.accuse(command(USER, "hate", null)), AccuseErrorCode.ACCUSE_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("대상 테이블 이름이 비었으면 호출한 도메인의 잘못이라 IllegalArgumentException이고 저장하지 않는다")
    void 빈_대상() {
        assertThatThrownBy(() -> accuseService.accuse(new CreateAccuseCommand(" ", MEME_ID, OWNER, USER, "hate", null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accuses", Long.class)).isZero();
    }

    private static void assertError(ThrowingCallable call, ErrorCode errorCode) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(errorCode);
    }
}
