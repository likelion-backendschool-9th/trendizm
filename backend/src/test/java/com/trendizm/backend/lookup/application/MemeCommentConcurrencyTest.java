package com.trendizm.backend.lookup.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.error.ErrorCode;
import com.trendizm.backend.lookup.application.dto.CreateMemeCommentCommand;
import com.trendizm.backend.lookup.application.dto.UpdateMemeCommentCommand;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeCommentService;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import com.trendizm.backend.member.application.port.MemberService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 댓글 삭제가 커밋되기 전에 같은 댓글의 수정·좋아요·답글 요청이 들어올 때를 검사한다.
 * 삭제 트랜잭션이 댓글 행을 잠근 채 잠시 멈춰 있는 동안 다른 요청을 보내고, 그 요청이 삭제가 끝날 때까지 기다렸다가
 * 삭제된 댓글로 보는지 확인한다. 잠그지 않고 읽으면 삭제 전 상태를 읽어서 댓글이 되살아나거나 좋아요가 남는다.
 * 스레드마다 트랜잭션이 커밋되어야 해서 @Transactional을 붙이지 않고, 테스트가 끝나면 테이블을 비운다.
 */
@SpringBootTest
@Sql(statements = {"DELETE FROM meme_comment_likes", "DELETE FROM meme_comments", "DELETE FROM meme_interpretations",
        "DELETE FROM memes", "DELETE FROM users"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MemeCommentConcurrencyTest {

    /** 삭제 트랜잭션이 잠금을 쥔 채 기다리는 시간. 다른 요청이 그 사이에 들어와 잠금을 기다리게 된다 */
    private static final long HOLD_MILLIS = 300;

    @Autowired
    private MemeCommentService commentService;

    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private MemeInterpretationRepository interpretationRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long writer;
    private Long reader;
    private Long interpretationId;
    private Long commentId;

    @BeforeEach
    void setUp() {
        writer = memberService.register(new RegisterMemberCommand("writer@test.com", "encoded", "밈장인", false));
        reader = memberService.register(new RegisterMemberCommand("reader@test.com", "encoded", "TMI러", false));
        Long memeId = memeRepository.save(Meme.create("TMI", writer, "https://youtu.be/tmi01", null)).getId();
        interpretationId = interpretationRepository.save(
                MemeInterpretation.create(memeId, writer, "Too Much Information", "내용")).getId();
        commentId = commentService.createComment(
                new CreateMemeCommentCommand(interpretationId, null, writer, "지울 댓글")).id();
    }

    @Test
    @DisplayName("삭제가 커밋되기 전에 수정이 들어와도, 수정은 삭제를 기다렸다가 MEME_COMMENT_NOT_FOUND이고 댓글은 되살아나지 않는다")
    void 수정과_삭제() throws Exception {
        whileDeleting(() -> commentService.updateComment(new UpdateMemeCommentCommand(commentId, writer, "부활")),
                MemeErrorCode.MEME_COMMENT_NOT_FOUND);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT deleted_at IS NOT NULL FROM meme_comments WHERE id = ?", Boolean.class, commentId)).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT content FROM meme_comments WHERE id = ?", String.class, commentId)).isEqualTo("지울 댓글");
    }

    @Test
    @DisplayName("삭제가 커밋되기 전에 좋아요가 들어와도, 좋아요는 삭제를 기다렸다가 MEME_COMMENT_NOT_FOUND이고 좋아요는 남지 않는다")
    void 좋아요와_삭제() throws Exception {
        whileDeleting(() -> commentService.likeComment(commentId, reader), MemeErrorCode.MEME_COMMENT_NOT_FOUND);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM meme_comment_likes WHERE comment_id = ?", Long.class, commentId)).isZero();
    }

    @Test
    @DisplayName("삭제가 커밋되기 전에 답글이 들어와도, 답글은 삭제를 기다렸다가 MEME_INVALID_PARENT_COMMENT이고 달리지 않는다")
    void 답글과_삭제() throws Exception {
        whileDeleting(() -> commentService.createComment(
                        new CreateMemeCommentCommand(interpretationId, commentId, reader, "답글")),
                MemeErrorCode.MEME_INVALID_PARENT_COMMENT);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM meme_comments WHERE parent_id = ?", Long.class, commentId)).isZero();
    }

    /**
     * 다른 스레드에서 댓글을 지우고 커밋하기 전에 잠시 멈춘다. 그 사이 이 스레드에서 request를 보내고 errorCode로 실패하는지 본다.
     */
    private void whileDeleting(ThrowingCallable request, ErrorCode errorCode) throws Exception {
        CountDownLatch deleted = new CountDownLatch(1);
        CompletableFuture<Void> deleting = CompletableFuture.runAsync(() -> transactionTemplate.executeWithoutResult(status -> {
            commentService.deleteComment(commentId, writer);
            deleted.countDown();
            sleep();
        }));
        assertThat(deleted.await(10, TimeUnit.SECONDS)).isTrue();

        assertThatThrownBy(request)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(errorCode);
        deleting.get(10, TimeUnit.SECONDS);
    }

    private static void sleep() {
        try {
            Thread.sleep(HOLD_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
