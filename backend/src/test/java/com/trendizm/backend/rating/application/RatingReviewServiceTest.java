package com.trendizm.backend.rating.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import com.trendizm.backend.member.application.port.MemberService;
import com.trendizm.backend.point.application.port.PointHistoryFilter;
import com.trendizm.backend.point.application.port.PointService;
import com.trendizm.backend.rating.application.dto.RatingReviewQuery;
import com.trendizm.backend.rating.application.dto.RatingReviewResponse;
import com.trendizm.backend.rating.application.dto.RatingReviewSaveResult;
import com.trendizm.backend.rating.application.dto.RegisterRatingContentCommand;
import com.trendizm.backend.rating.application.dto.SaveRatingReviewCommand;
import com.trendizm.backend.rating.application.exception.RatingErrorCode;
import com.trendizm.backend.rating.application.port.RatingService;
import com.trendizm.backend.rating.application.port.out.RatingRepository;
import com.trendizm.backend.rating.application.port.ReviewSort;
import com.trendizm.backend.rating.domain.enums.Sentiment;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RatingReviewServiceTest {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PointService pointService;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private EntityManager entityManager;

    private Long contentId;
    private Long alice;
    private Long bob;
    private Long carol;

    @BeforeEach
    void setUp() {
        contentId = ratingService.registerContent(new RegisterRatingContentCommand("drama",
                "ongoing", "드라마", null, null, null, null, null)).id();
        alice = memberService.register(new RegisterMemberCommand("alice@test.com", "encoded", "앨리스", false));
        bob = memberService.register(new RegisterMemberCommand("bob@test.com", "encoded", "밥", false));
        carol = memberService.register(new RegisterMemberCommand("carol@test.com", "encoded", "캐롤", false));
    }

    private RatingReviewSaveResult save(Long userId, Sentiment sentiment, int score) {
        return ratingService.saveReview(new SaveRatingReviewCommand(contentId, userId, sentiment, score,
                "제목", "본문", null));
    }

    private List<Long> reviewIds(Sentiment sentiment, ReviewSort sort) {
        return ratingService.getReviews(new RatingReviewQuery(contentId, sentiment, sort, null, 0, 20))
                .content().stream().map(RatingReviewResponse::id).toList();
    }

    @Test
    @DisplayName("처음 후기를 쓰면 새로 만들고 10P를 적립한다")
    void 첫_후기() {
        RatingReviewSaveResult result = ratingService.saveReview(new SaveRatingReviewCommand(contentId, alice,
                Sentiment.LIKE, 5, "최고", "재밌어요", 8));

        assertThat(result.created()).isTrue();
        assertThat(result.rewardPoint()).isEqualTo(10);
        assertThat(result.review().sentiment().slug()).isEqualTo("like");
        assertThat(result.review().sentiment().label()).isEqualTo("좋아요");
        assertThat(result.review().progress()).isEqualTo(8);
        assertThat(result.review().authorNickname()).isEqualTo("앨리스");
        assertThat(result.review().mine()).isTrue();
        assertThat(result.review().createdAt()).isNotNull();
        assertThat(pointService.getBalance(alice).balance()).isEqualTo(10);
        assertThat(pointService.getHistories(alice, PointHistoryFilter.EARN, 0, 20).content())
                .singleElement().satisfies(history -> assertThat(history.type()).isEqualTo("REWARD"));
    }

    @Test
    @DisplayName("이미 후기가 있으면 수정하고, 보상은 다시 주지 않는다")
    void 후기_수정() {
        Long reviewId = save(alice, Sentiment.LIKE, 5).review().id();

        RatingReviewSaveResult updated = ratingService.saveReview(new SaveRatingReviewCommand(contentId, alice,
                Sentiment.DISLIKE, 2, "다시 보니 별로", "뒤로 갈수록 아쉬워요", null));

        assertThat(updated.created()).isFalse();
        assertThat(updated.rewardPoint()).isZero();
        assertThat(updated.review().id()).isEqualTo(reviewId);
        assertThat(updated.review().sentiment().slug()).isEqualTo("dislike");
        assertThat(updated.review().score()).isEqualTo(2);
        assertThat(updated.review().title()).isEqualTo("다시 보니 별로");
        assertThat(pointService.getBalance(alice).balance()).isEqualTo(10);
        assertThat(ratingService.getReviews(new RatingReviewQuery(contentId, null, null, null, 0, 20))
                .totalElements()).isEqualTo(1);
        assertThat(ratingService.getContent(contentId).stats().dislikeCount())
                .as("수정한 호/불호가 집계에 반영된다").isEqualTo(1);
    }

    @Test
    @DisplayName("후기 목록은 최신순이 기본이고, 호/불호로 거르고 별점순으로 정렬한다")
    void 후기_목록() {
        Long aliceReview = save(alice, Sentiment.LIKE, 4).review().id();
        Long bobReview = save(bob, Sentiment.LIKE, 5).review().id();
        Long carolReview = save(carol, Sentiment.DISLIKE, 1).review().id();

        assertThat(reviewIds(null, null)).containsExactly(carolReview, bobReview, aliceReview);
        assertThat(reviewIds(Sentiment.LIKE, ReviewSort.LATEST)).containsExactly(bobReview, aliceReview);
        assertThat(reviewIds(null, ReviewSort.SCORE_HIGH)).containsExactly(bobReview, aliceReview, carolReview);
        assertThat(reviewIds(null, ReviewSort.SCORE_LOW)).containsExactly(carolReview, aliceReview, bobReview);
    }

    @Test
    @DisplayName("같은 별점이면 최신 후기가 먼저 온다")
    void 같은_별점() {
        Long aliceReview = save(alice, Sentiment.LIKE, 5).review().id();
        Long bobReview = save(bob, Sentiment.LIKE, 5).review().id();

        assertThat(reviewIds(null, ReviewSort.SCORE_HIGH)).containsExactly(bobReview, aliceReview);
    }

    @Test
    @DisplayName("후기 목록에 작성자 닉네임과 내 후기인지를 담는다")
    void 후기_작성자() {
        save(alice, Sentiment.LIKE, 5);

        RatingReviewResponse seenByAlice = ratingService.getReviews(
                new RatingReviewQuery(contentId, null, null, alice, 0, 20)).content().get(0);
        RatingReviewResponse seenByGuest = ratingService.getReviews(
                new RatingReviewQuery(contentId, null, null, null, 0, 20)).content().get(0);

        assertThat(seenByAlice.authorNickname()).isEqualTo("앨리스");
        assertThat(seenByAlice.mine()).isTrue();
        assertThat(seenByGuest.mine()).isFalse();
    }

    @Test
    @DisplayName("숨긴 콘텐츠에는 후기를 쓰거나 볼 수 없다 (RATING_CONTENT_NOT_FOUND)")
    void 숨긴_콘텐츠() {
        ratingService.changeContentVisibility(contentId, false);

        for (Runnable call : List.<Runnable>of(
                () -> save(alice, Sentiment.LIKE, 5),
                () -> ratingService.getReviews(new RatingReviewQuery(contentId, null, null, null, 0, 20)))) {
            assertThatThrownBy(call::run)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(RatingErrorCode.RATING_CONTENT_NOT_FOUND);
        }
        assertThat(pointService.getBalance(alice).balance()).isZero();
    }

    @Test
    @DisplayName("별점이 1~5가 아니면 저장하지 않는다")
    void 잘못된_별점() {
        assertThatThrownBy(() -> save(alice, Sentiment.LIKE, 6)).isInstanceOf(IllegalArgumentException.class);
        assertThat(ratingService.getReviews(new RatingReviewQuery(contentId, null, null, null, 0, 20))
                .totalElements()).isZero();
    }

    @Test
    @DisplayName("후기 공감은 한 사람당 한 번이고, 공감 수와 내가 공감했는지를 보여 준다")
    void 후기_공감() {
        Long reviewId = save(alice, Sentiment.LIKE, 5).review().id();

        ratingService.likeReview(reviewId, bob);
        ratingService.likeReview(reviewId, bob);
        ratingService.likeReview(reviewId, carol);

        RatingReviewResponse seenByBob = ratingService.getReviews(
                new RatingReviewQuery(contentId, null, null, bob, 0, 20)).content().get(0);
        assertThat(seenByBob.likeCount()).isEqualTo(2);
        assertThat(seenByBob.likedByMe()).isTrue();

        RatingReviewResponse seenByGuest = ratingService.getReviews(
                new RatingReviewQuery(contentId, null, null, null, 0, 20)).content().get(0);
        assertThat(seenByGuest.likeCount()).isEqualTo(2);
        assertThat(seenByGuest.likedByMe()).isFalse();
    }

    @Test
    @DisplayName("공감을 취소하면 공감 수가 줄고, 공감하지 않은 후기를 취소해도 오류가 나지 않는다")
    void 후기_공감_취소() {
        Long reviewId = save(alice, Sentiment.LIKE, 5).review().id();
        ratingService.likeReview(reviewId, bob);

        ratingService.unlikeReview(reviewId, bob);
        ratingService.unlikeReview(reviewId, bob);

        RatingReviewResponse review = ratingService.getReviews(
                new RatingReviewQuery(contentId, null, null, bob, 0, 20)).content().get(0);
        assertThat(review.likeCount()).isZero();
        assertThat(review.likedByMe()).isFalse();
    }

    @Test
    @DisplayName("공감 많은 순으로 정렬하고, 공감 수가 같으면 최신 후기가 먼저 온다")
    void 공감순_정렬() {
        Long aliceReview = save(alice, Sentiment.LIKE, 5).review().id();
        Long bobReview = save(bob, Sentiment.DISLIKE, 2).review().id();
        Long carolReview = save(carol, Sentiment.LIKE, 4).review().id();
        ratingService.likeReview(bobReview, alice);
        ratingService.likeReview(bobReview, carol);
        ratingService.likeReview(aliceReview, bob);

        assertThat(reviewIds(null, ReviewSort.LIKES)).containsExactly(bobReview, aliceReview, carolReview);
        assertThat(reviewIds(Sentiment.LIKE, ReviewSort.LIKES)).containsExactly(aliceReview, carolReview);
        assertThat(ratingService.getReviews(new RatingReviewQuery(contentId, null, ReviewSort.LIKES, null, 0, 2))
                .totalElements()).as("페이지 나눔에도 전체 개수는 그대로").isEqualTo(3);
    }

    @Test
    @DisplayName("없는 후기나 숨긴 콘텐츠의 후기에 공감하면 RATING_REVIEW_NOT_FOUND")
    void 공감_대상_없음() {
        Long reviewId = save(alice, Sentiment.LIKE, 5).review().id();
        ratingService.changeContentVisibility(contentId, false);

        for (Long target : List.of(999_999L, reviewId)) {
            assertThatThrownBy(() -> ratingService.likeReview(target, bob))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(RatingErrorCode.RATING_REVIEW_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("후기를 수정해도 받은 공감은 그대로 남는다")
    void 수정_후_공감() {
        Long reviewId = save(alice, Sentiment.LIKE, 5).review().id();
        ratingService.likeReview(reviewId, bob);

        RatingReviewSaveResult updated = save(alice, Sentiment.DISLIKE, 1);

        assertThat(updated.review().likeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("후기를 수정하면 응답의 수정 시각이 DB에 저장된 새 수정 시각과 같다 (DB 정밀도 차이는 1ms까지 허용)")
    void 수정_시각() throws InterruptedException {
        RatingReviewSaveResult created = save(alice, Sentiment.LIKE, 5);
        entityManager.flush();
        Thread.sleep(10); // 수정 시각이 처음 값과 달라지도록 잠깐 기다린다

        RatingReviewSaveResult updated = save(alice, Sentiment.DISLIKE, 1);
        entityManager.clear();

        LocalDateTime saved = ratingRepository.findById(updated.review().id()).orElseThrow().getUpdatedAt();
        // Linux JVM은 나노초까지, DB(timestamp)는 마이크로초까지 저장해서 끝자리가 다를 수 있다.
        // 고치기 전에는 수정 전 시각(10ms 이상 차이)이 나갔으므로 1ms 허용으로도 그 문제는 잡힌다
        assertThat(updated.review().updatedAt()).isCloseTo(saved, within(1, ChronoUnit.MILLIS));
        assertThat(updated.review().updatedAt()).isAfter(created.review().updatedAt());
    }
}
