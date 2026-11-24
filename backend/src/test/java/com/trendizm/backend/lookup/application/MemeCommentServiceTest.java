package com.trendizm.backend.lookup.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.error.ErrorCode;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.CreateMemeCommentCommand;
import com.trendizm.backend.lookup.application.dto.MemeCommentResponse;
import com.trendizm.backend.lookup.application.dto.UpdateMemeCommentCommand;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeCommentService;
import com.trendizm.backend.lookup.application.port.out.MemeCommentRepository;
import com.trendizm.backend.lookup.application.port.out.MemeInterpretationRepository;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.entity.MemeInterpretation;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import com.trendizm.backend.member.application.port.MemberService;
import java.util.List;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemeCommentServiceTest {

    @Autowired
    private MemeCommentService commentService;

    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private MemeInterpretationRepository interpretationRepository;

    @Autowired
    private MemeCommentRepository commentRepository;

    @Autowired
    private MemberService memberService;

    private Long writer;
    private Long reader;
    private Long interpretationId;
    private Long otherInterpretationId;
    private Long hiddenInterpretationId;

    /** 회원 2명, 공개 문서의 의견 2개, 숨김 문서의 의견 1개. 테스트가 끝나면 롤백된다 */
    @BeforeEach
    void setUp() {
        writer = memberService.register(new RegisterMemberCommand("writer@test.com", "encoded", "밈장인", false));
        reader = memberService.register(new RegisterMemberCommand("reader@test.com", "encoded", "TMI러", false));
        Long memeId = memeRepository.save(Meme.create("TMI", writer, "https://youtu.be/tmi01", null, null)).getId();
        interpretationId = interpretationRepository.save(
                MemeInterpretation.create(memeId, writer, "Too Much Information", "내용")).getId();
        otherInterpretationId = interpretationRepository.save(
                MemeInterpretation.create(memeId, writer, "다른 뜻", "내용")).getId();

        Meme hidden = Meme.create("알숨김", writer, "https://example.com/hidden", null, null);
        hidden.hide();
        Long hiddenMemeId = memeRepository.save(hidden).getId();
        hiddenInterpretationId = interpretationRepository.save(
                MemeInterpretation.create(hiddenMemeId, writer, "숨김 의견", "내용")).getId();
    }

    private MemeCommentResponse comment(Long userId, String content) {
        return commentService.createComment(new CreateMemeCommentCommand(interpretationId, null, userId, content));
    }

    private MemeCommentResponse reply(Long parentId, Long userId, String content) {
        return commentService.createComment(new CreateMemeCommentCommand(interpretationId, parentId, userId, content));
    }

    private List<MemeCommentResponse> comments(Long viewerId) {
        return commentService.getComments(interpretationId, viewerId, 0, 20).content();
    }

    private static void assertError(ThrowingCallable call, ErrorCode errorCode) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(errorCode);
    }

    @Test
    @DisplayName("댓글은 최신순, 대댓글은 댓글마다 오래된 순으로 담고 작성자 닉네임을 붙인다")
    void 댓글_목록() {
        MemeCommentResponse first = comment(writer, "첫 댓글");
        MemeCommentResponse second = comment(reader, "둘째 댓글");
        MemeCommentResponse olderReply = reply(first.id(), reader, "먼저 단 답글");
        MemeCommentResponse newerReply = reply(first.id(), writer, "나중 단 답글");

        List<MemeCommentResponse> list = comments(null);

        assertThat(list).extracting(MemeCommentResponse::id).containsExactly(second.id(), first.id());
        assertThat(list.get(0).authorNickname()).isEqualTo("TMI러");
        assertThat(list.get(1).replies()).extracting(MemeCommentResponse::id)
                .containsExactly(olderReply.id(), newerReply.id());
        assertThat(list.get(1).replies().get(0).parentId()).isEqualTo(first.id());
    }

    @Test
    @DisplayName("페이지는 댓글(대댓글 제외) 기준으로 나눈다")
    void 댓글_페이지() {
        MemeCommentResponse first = comment(writer, "1");
        comment(writer, "2");
        comment(writer, "3");
        reply(first.id(), reader, "답글");

        PageResponse<MemeCommentResponse> page = commentService.getComments(interpretationId, null, 1, 2);

        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.content()).extracting(MemeCommentResponse::id).containsExactly(first.id());
        assertThat(page.content().get(0).replies()).hasSize(1);
    }

    @Test
    @DisplayName("대댓글은 같은 의견의 댓글에만 달 수 있고, 대댓글·다른 의견의 댓글·없는 댓글이면 MEME_INVALID_PARENT_COMMENT")
    void 잘못된_부모_댓글() {
        MemeCommentResponse parent = comment(writer, "댓글");
        MemeCommentResponse child = reply(parent.id(), reader, "답글");
        MemeCommentResponse other = commentService.createComment(
                new CreateMemeCommentCommand(otherInterpretationId, null, writer, "다른 의견 댓글"));

        for (Long parentId : List.of(child.id(), other.id(), -1L)) {
            assertError(() -> reply(parentId, reader, "답글"), MemeErrorCode.MEME_INVALID_PARENT_COMMENT);
        }
    }

    @Test
    @DisplayName("없는 의견이나 숨김 문서의 의견에는 댓글을 보거나 쓸 수 없다 (MEME_INTERPRETATION_NOT_FOUND)")
    void 의견_없음() {
        for (Long id : List.of(-1L, hiddenInterpretationId)) {
            assertError(() -> commentService.getComments(id, null, 0, 20), MemeErrorCode.MEME_INTERPRETATION_NOT_FOUND);
            assertError(() -> commentService.createComment(new CreateMemeCommentCommand(id, null, writer, "댓글")),
                    MemeErrorCode.MEME_INTERPRETATION_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("좋아요는 한 사람이 한 번만 세고, 로그인한 사람에게만 likedByMe가 나오고, 취소하면 빠진다")
    void 좋아요() {
        MemeCommentResponse target = comment(writer, "댓글");

        commentService.likeComment(target.id(), reader);
        commentService.likeComment(target.id(), reader);
        commentService.likeComment(target.id(), writer);

        assertThat(comments(reader).get(0).likeCount()).isEqualTo(2);
        assertThat(comments(reader).get(0).likedByMe()).isTrue();
        assertThat(comments(null).get(0).likedByMe()).isFalse();

        commentService.unlikeComment(target.id(), reader);
        commentService.unlikeComment(target.id(), reader);

        assertThat(comments(reader).get(0).likeCount()).isEqualTo(1);
        assertThat(comments(reader).get(0).likedByMe()).isFalse();
    }

    @Test
    @DisplayName("작성자는 댓글을 고칠 수 있고, 남의 댓글이면 MEME_COMMENT_FORBIDDEN, 없는 댓글이면 MEME_COMMENT_NOT_FOUND")
    void 댓글_수정() {
        MemeCommentResponse target = comment(writer, "처음 내용");

        MemeCommentResponse updated = commentService.updateComment(
                new UpdateMemeCommentCommand(target.id(), writer, "고친 내용"));

        assertThat(updated.content()).isEqualTo("고친 내용");
        assertThat(updated.updatedAt()).isAfterOrEqualTo(target.updatedAt());
        assertThat(comments(null).get(0).content()).isEqualTo("고친 내용");
        assertError(() -> commentService.updateComment(new UpdateMemeCommentCommand(target.id(), reader, "남의 수정")),
                MemeErrorCode.MEME_COMMENT_FORBIDDEN);
        assertError(() -> commentService.updateComment(new UpdateMemeCommentCommand(-1L, writer, "없는 댓글")),
                MemeErrorCode.MEME_COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("대댓글이 없는 댓글을 지우면 목록에서 사라지고, 남의 댓글은 지울 수 없다")
    void 댓글_삭제() {
        MemeCommentResponse target = comment(writer, "댓글");
        commentService.likeComment(target.id(), reader);

        assertError(() -> commentService.deleteComment(target.id(), reader), MemeErrorCode.MEME_COMMENT_FORBIDDEN);
        commentService.deleteComment(target.id(), writer);

        assertThat(comments(null)).isEmpty();
        assertThat(commentRepository.findById(target.id())).isEmpty();
    }

    @Test
    @DisplayName("대댓글이 있는 댓글을 지우면 '삭제된 댓글'로 흔적만 남고 대댓글은 그대로다. 그 댓글은 수정·좋아요·답글이 안 된다")
    void 대댓글_있는_댓글_삭제() {
        MemeCommentResponse parent = comment(writer, "지울 댓글");
        MemeCommentResponse child = reply(parent.id(), reader, "남의 답글");
        commentService.likeComment(parent.id(), reader);

        commentService.deleteComment(parent.id(), writer);

        MemeCommentResponse shown = comments(reader).get(0);
        assertThat(shown.deleted()).isTrue();
        assertThat(shown.content()).isNull();
        assertThat(shown.userId()).isNull();
        assertThat(shown.authorNickname()).isNull();
        assertThat(shown.likeCount()).isZero();
        assertThat(shown.replies()).extracting(MemeCommentResponse::id).containsExactly(child.id());

        assertError(() -> commentService.updateComment(new UpdateMemeCommentCommand(parent.id(), writer, "부활")),
                MemeErrorCode.MEME_COMMENT_NOT_FOUND);
        assertError(() -> commentService.likeComment(parent.id(), reader), MemeErrorCode.MEME_COMMENT_NOT_FOUND);
        assertError(() -> commentService.deleteComment(parent.id(), writer), MemeErrorCode.MEME_COMMENT_NOT_FOUND);
        assertError(() -> reply(parent.id(), reader, "또 답글"), MemeErrorCode.MEME_INVALID_PARENT_COMMENT);
    }

    @Test
    @DisplayName("흔적만 남은 댓글의 마지막 대댓글을 지우면 그 댓글도 함께 지워진다")
    void 마지막_대댓글_삭제() {
        MemeCommentResponse parent = comment(writer, "지울 댓글");
        MemeCommentResponse first = reply(parent.id(), reader, "답글 1");
        MemeCommentResponse second = reply(parent.id(), writer, "답글 2");
        commentService.deleteComment(parent.id(), writer);

        commentService.deleteComment(first.id(), reader);
        assertThat(comments(null)).singleElement().satisfies(shown -> {
            assertThat(shown.deleted()).isTrue();
            assertThat(shown.replies()).extracting(MemeCommentResponse::id).containsExactly(second.id());
        });

        commentService.deleteComment(second.id(), writer);
        assertThat(comments(null)).isEmpty();
        assertThat(commentRepository.findById(parent.id())).isEmpty();
    }

    @Test
    @DisplayName("삭제 표시되지 않은 댓글의 대댓글을 지우면 대댓글만 사라진다")
    void 대댓글_삭제() {
        MemeCommentResponse parent = comment(writer, "댓글");
        MemeCommentResponse child = reply(parent.id(), reader, "답글");

        commentService.deleteComment(child.id(), reader);

        assertThat(comments(null)).singleElement().satisfies(shown -> {
            assertThat(shown.deleted()).isFalse();
            assertThat(shown.replies()).isEmpty();
        });
    }
}
