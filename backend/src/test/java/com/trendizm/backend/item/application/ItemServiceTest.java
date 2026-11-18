package com.trendizm.backend.item.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.error.ErrorCode;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.item.application.dto.CreateItemCommand;
import com.trendizm.backend.item.application.dto.CreateItemCommentCommand;
import com.trendizm.backend.item.application.dto.CreateItemQuestionCommand;
import com.trendizm.backend.item.application.dto.ItemCommentResponse;
import com.trendizm.backend.item.application.dto.ItemQuestionQuery;
import com.trendizm.backend.item.application.dto.ItemQuestionResponse;
import com.trendizm.backend.item.application.dto.ItemQuestionSummaryResponse;
import com.trendizm.backend.item.application.dto.ItemResponse;
import com.trendizm.backend.item.application.exception.ItemErrorCode;
import com.trendizm.backend.item.application.port.ItemService;
import com.trendizm.backend.item.domain.enums.ItemCategory;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;
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
class ItemServiceTest {

    /** 회원 테이블에 없는 id (탈퇴 등으로 찾을 수 없는 회원) */
    private static final Long UNKNOWN_MEMBER = 999_999L;

    @Autowired
    private ItemService itemService;

    @Autowired
    private MemberService memberService;

    private Long asker;
    private Long recommender;

    @BeforeEach
    void setUp() {
        asker = memberService.register(new RegisterMemberCommand("asker@test.com", "encoded", "질문자", false));
        recommender = memberService.register(new RegisterMemberCommand("rec@test.com", "encoded", "추천자", false));
    }

    private Long question(ItemSubcategory subcategory) {
        return itemService.createQuestion(
                new CreateItemQuestionCommand(asker, subcategory, "양말 추천해주세요", "따뜻한 양말을 찾아요")).id();
    }

    private Long comment(Long questionId, Long parentId, Long userId) {
        return itemService.createComment(new CreateItemCommentCommand(questionId, parentId, userId, "댓글")).id();
    }

    private ItemResponse recommend(Long questionId, Long userId) {
        return itemService.createRecommendation(new CreateItemCommand(questionId, userId, "포근 양말", "무신사",
                12000, List.of("화이트", "폭신한 재질"), "정말 따뜻해요", "https://img.test/a.jpg",
                "https://shop.test/a"));
    }

    private static void assertError(ThrowingCallable call, ErrorCode expected) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("질문 글을 만들면 소분류로 대분류가 정해지고, 작성자에게는 mine = true다")
    void 질문_작성() {
        ItemQuestionResponse created = itemService.createQuestion(
                new CreateItemQuestionCommand(asker, ItemSubcategory.SHOES, "운동화", "추천해 주세요"));

        assertThat(created.category().code()).isEqualTo("FASHION");
        assertThat(created.subcategory().slug()).isEqualTo("shoes");
        assertThat(created.subcategory().label()).isEqualTo("신발");
        assertThat(created.authorNickname()).isEqualTo("질문자");
        assertThat(created.mine()).isTrue();
        assertThat(itemService.getQuestion(created.id(), null).mine()).isFalse();
        assertThat(itemService.getQuestion(created.id(), recommender).mine()).isFalse();
    }

    @Test
    @DisplayName("목록은 공개 글만 최신순으로 보여 주고, 대분류·소분류로 거른다")
    void 질문_목록() {
        Long socks = question(ItemSubcategory.CLOTHING);
        Long shoes = question(ItemSubcategory.SHOES);
        Long perfume = question(ItemSubcategory.PERFUME);
        Long hidden = question(ItemSubcategory.CLOTHING);
        itemService.changeQuestionVisibility(hidden, false);

        assertThat(itemService.getQuestions(new ItemQuestionQuery(null, null, 0, 20)).content())
                .extracting(ItemQuestionSummaryResponse::id)
                .containsExactly(perfume, shoes, socks);
        assertThat(itemService.getQuestions(new ItemQuestionQuery(ItemCategory.FASHION, null, 0, 20)).content())
                .extracting(ItemQuestionSummaryResponse::id)
                .containsExactly(shoes, socks);
        assertThat(itemService.getQuestions(
                new ItemQuestionQuery(ItemCategory.FASHION, ItemSubcategory.CLOTHING, 0, 20)).content())
                .extracting(ItemQuestionSummaryResponse::id)
                .containsExactly(socks);
    }

    @Test
    @DisplayName("목록에 작성자 닉네임, 댓글 수(대댓글 포함), 추천 아이템 수를 담는다")
    void 질문_목록_개수() {
        Long questionId = question(ItemSubcategory.CLOTHING);
        Long parent = comment(questionId, null, recommender);
        comment(questionId, parent, asker);
        recommend(questionId, recommender);

        PageResponse<ItemQuestionSummaryResponse> page = itemService.getQuestions(new ItemQuestionQuery(null, null, 0, 20));

        assertThat(page.content()).singleElement().satisfies(summary -> {
            assertThat(summary.authorNickname()).isEqualTo("질문자");
            assertThat(summary.commentCount()).isEqualTo(2);
            assertThat(summary.recommendationCount()).isEqualTo(1);
            assertThat(summary.contentPreview()).isEqualTo("따뜻한 양말을 찾아요");
        });
    }

    @Test
    @DisplayName("대분류와 맞지 않는 소분류로 거르면 ITEM_CATEGORY_MISMATCH")
    void 카테고리_불일치() {
        assertError(() -> itemService.getQuestions(
                        new ItemQuestionQuery(ItemCategory.BEAUTY, ItemSubcategory.CLOTHING, 0, 20)),
                ItemErrorCode.ITEM_CATEGORY_MISMATCH);
    }

    @Test
    @DisplayName("숨긴 글은 상세·댓글·추천 조회와 쓰기에서 ITEM_QUESTION_NOT_FOUND이고, 숨김을 풀면 다시 보인다")
    void 숨긴_글() {
        Long questionId = question(ItemSubcategory.CLOTHING);
        itemService.changeQuestionVisibility(questionId, false);

        assertError(() -> itemService.getQuestion(questionId, null), ItemErrorCode.ITEM_QUESTION_NOT_FOUND);
        assertError(() -> itemService.getComments(questionId, null, 0, 20), ItemErrorCode.ITEM_QUESTION_NOT_FOUND);
        assertError(() -> itemService.getRecommendations(questionId, null), ItemErrorCode.ITEM_QUESTION_NOT_FOUND);
        assertError(() -> comment(questionId, null, recommender), ItemErrorCode.ITEM_QUESTION_NOT_FOUND);
        assertError(() -> recommend(questionId, recommender), ItemErrorCode.ITEM_QUESTION_NOT_FOUND);

        itemService.changeQuestionVisibility(questionId, true);
        assertThat(itemService.getQuestion(questionId, null).id()).isEqualTo(questionId);
    }

    @Test
    @DisplayName("없는 글을 숨기면 ITEM_QUESTION_NOT_FOUND")
    void 없는_글_숨김() {
        assertError(() -> itemService.changeQuestionVisibility(UNKNOWN_MEMBER, false),
                ItemErrorCode.ITEM_QUESTION_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 목록은 최신순이고, 댓글마다 대댓글을 오래된 순으로 담는다")
    void 댓글_목록() {
        Long questionId = question(ItemSubcategory.CLOTHING);
        Long first = comment(questionId, null, recommender);
        Long second = comment(questionId, null, asker);
        Long reply = comment(questionId, first, asker);
        Long secondReply = comment(questionId, first, recommender);

        PageResponse<ItemCommentResponse> comments = itemService.getComments(questionId, null, 0, 20);

        assertThat(comments.totalElements()).as("page는 댓글(대댓글 제외) 기준").isEqualTo(2);
        assertThat(comments.content()).extracting(ItemCommentResponse::id).containsExactly(second, first);
        assertThat(comments.content().get(0).replies()).isEmpty();
        ItemCommentResponse firstComment = comments.content().get(1);
        assertThat(firstComment.authorNickname()).isEqualTo("추천자");
        assertThat(firstComment.replies()).extracting(ItemCommentResponse::id).containsExactly(reply, secondReply);
        assertThat(firstComment.replies().get(0).parentId()).isEqualTo(first);
        assertThat(firstComment.replies().get(0).authorNickname()).isEqualTo("질문자");
    }

    @Test
    @DisplayName("대댓글에 답글을 달거나 다른 글의 댓글에 답글을 달면 ITEM_INVALID_PARENT_COMMENT")
    void 잘못된_대댓글() {
        Long questionId = question(ItemSubcategory.CLOTHING);
        Long other = question(ItemSubcategory.SHOES);
        Long parent = comment(questionId, null, recommender);
        Long reply = comment(questionId, parent, asker);

        assertError(() -> comment(questionId, reply, recommender), ItemErrorCode.ITEM_INVALID_PARENT_COMMENT);
        assertError(() -> comment(other, parent, recommender), ItemErrorCode.ITEM_INVALID_PARENT_COMMENT);
        assertError(() -> comment(questionId, UNKNOWN_MEMBER, recommender), ItemErrorCode.ITEM_INVALID_PARENT_COMMENT);
    }

    @Test
    @DisplayName("댓글 공감은 한 사람당 한 번이고, 공감 수와 내가 공감했는지를 보여 준다")
    void 댓글_공감() {
        Long questionId = question(ItemSubcategory.CLOTHING);
        Long commentId = comment(questionId, null, recommender);
        Long replyId = comment(questionId, commentId, recommender);

        itemService.likeComment(commentId, asker);
        itemService.likeComment(commentId, asker);
        itemService.likeComment(commentId, recommender);
        itemService.likeComment(replyId, asker);

        ItemCommentResponse seenByAsker = itemService.getComments(questionId, asker, 0, 20).content().get(0);
        assertThat(seenByAsker.likeCount()).isEqualTo(2);
        assertThat(seenByAsker.likedByMe()).isTrue();
        assertThat(seenByAsker.replies().get(0).likeCount()).isEqualTo(1);
        assertThat(seenByAsker.replies().get(0).likedByMe()).isTrue();

        ItemCommentResponse seenByGuest = itemService.getComments(questionId, null, 0, 20).content().get(0);
        assertThat(seenByGuest.likedByMe()).isFalse();
    }

    @Test
    @DisplayName("공감을 취소하면 공감 수가 줄고, 공감하지 않은 댓글을 취소해도 오류가 나지 않는다")
    void 댓글_공감_취소() {
        Long questionId = question(ItemSubcategory.CLOTHING);
        Long commentId = comment(questionId, null, recommender);
        itemService.likeComment(commentId, asker);

        itemService.unlikeComment(commentId, asker);
        itemService.unlikeComment(commentId, asker);

        ItemCommentResponse comment = itemService.getComments(questionId, asker, 0, 20).content().get(0);
        assertThat(comment.likeCount()).isZero();
        assertThat(comment.likedByMe()).isFalse();
    }

    @Test
    @DisplayName("없는 댓글이나 숨긴 글의 댓글에 공감하면 ITEM_COMMENT_NOT_FOUND")
    void 공감_대상_없음() {
        Long questionId = question(ItemSubcategory.CLOTHING);
        Long commentId = comment(questionId, null, recommender);
        itemService.changeQuestionVisibility(questionId, false);

        assertError(() -> itemService.likeComment(UNKNOWN_MEMBER, asker), ItemErrorCode.ITEM_COMMENT_NOT_FOUND);
        assertError(() -> itemService.likeComment(commentId, asker), ItemErrorCode.ITEM_COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("추천 아이템은 등록자에게는 열린 상태, 다른 사람과 비로그인에게는 잠긴 상태로 보인다")
    void 추천_아이템_잠금() {
        Long questionId = question(ItemSubcategory.CLOTHING);
        ItemResponse created = recommend(questionId, recommender);
        assertThat(created.locked()).isFalse();
        assertThat(created.mine()).isTrue();

        ItemResponse own = itemService.getRecommendations(questionId, recommender).get(0);
        assertThat(own.locked()).isFalse();
        assertThat(own.title()).isEqualTo("포근 양말");
        assertThat(own.tags()).as("jsonb 칼럼에 저장한 태그").containsExactly("화이트", "폭신한 재질");

        for (Long viewer : new Long[] {asker, null}) {
            ItemResponse locked = itemService.getRecommendations(questionId, viewer).get(0);
            assertThat(locked.locked()).isTrue();
            assertThat(locked.mine()).isFalse();
            assertThat(locked.title()).isNull();
            assertThat(locked.seller()).isNull();
            assertThat(locked.description()).isNull();
            assertThat(locked.imageUrl()).isNull();
            assertThat(locked.linkUrl()).isNull();
            assertThat(locked.price()).isEqualTo(12000);
            assertThat(locked.tags()).containsExactly("화이트", "폭신한 재질");
            assertThat(locked.unlockPrice()).isEqualTo(100);
            assertThat(locked.authorNickname()).isEqualTo("추천자");
        }
    }

    @Test
    @DisplayName("회원 정보를 찾을 수 없는 작성자는 '탈퇴한 회원'으로 보인다")
    void 탈퇴한_회원() {
        Long questionId = question(ItemSubcategory.CLOTHING);
        comment(questionId, null, UNKNOWN_MEMBER);

        assertThat(itemService.getComments(questionId, null, 0, 20).content().get(0).authorNickname())
                .isEqualTo("탈퇴한 회원");
    }
}
