package com.trendizm.backend.rating.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.error.ErrorCode;
import com.trendizm.backend.rating.application.dto.RatingContentQuery;
import com.trendizm.backend.rating.application.dto.RatingContentResponse;
import com.trendizm.backend.rating.application.dto.RatingContentSummaryResponse;
import com.trendizm.backend.rating.application.dto.RegisterRatingContentCommand;
import com.trendizm.backend.rating.application.dto.SentimentStats;
import com.trendizm.backend.rating.application.exception.RatingErrorCode;
import com.trendizm.backend.rating.application.port.RatingService;
import com.trendizm.backend.rating.application.port.out.RatingRepository;
import com.trendizm.backend.rating.domain.entity.Rating;
import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import com.trendizm.backend.rating.domain.enums.Sentiment;
import java.time.LocalDate;
import java.util.Map;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** 후기 API는 다음 작업이라, 집계 확인용 후기는 RatingRepository로 직접 저장한다 */
@SpringBootTest
@Transactional
class RatingServiceTest {

    private static final Long UNKNOWN_CONTENT = 999_999L;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private RatingRepository ratingRepository;

    private Long register(RatingContentSubcategory subcategory, RatingContentStatus status) {
        return ratingService.registerContent(new RegisterRatingContentCommand(subcategory, status, "콘텐츠",
                "설명", "https://img.test/a.jpg", 15, LocalDate.of(2026, 11, 1), Map.of("season", 2))).id();
    }

    private void review(Long contentId, Long userId, Sentiment sentiment, int score) {
        ratingRepository.save(Rating.create(contentId, userId, sentiment, score, "제목", "본문", null));
    }

    private static void assertError(ThrowingCallable call, ErrorCode expected) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("콘텐츠를 등록하면 소분류로 대분류가 정해지고, 진행 상태 이름은 대분류에 맞게 나온다")
    void 콘텐츠_등록() {
        RatingContentResponse created = ratingService.registerContent(new RegisterRatingContentCommand(
                RatingContentSubcategory.DRAMA, RatingContentStatus.ONGOING, "드라마", null, null, null, null, null));

        assertThat(created.category().slug()).isEqualTo("media");
        assertThat(created.subcategory().slug()).isEqualTo("drama");
        assertThat(created.status().slug()).isEqualTo("ongoing");
        assertThat(created.status().label()).isEqualTo("방영중");
        assertThat(created.extra()).isEmpty();
        assertThat(created.stats()).isEqualTo(SentimentStats.EMPTY);
    }

    @Test
    @DisplayName("목록은 공개 콘텐츠만 최신 등록순으로 보여 주고, 대분류·소분류·진행 상태로 거른다")
    void 콘텐츠_목록() {
        Long drama = register(RatingContentSubcategory.DRAMA, RatingContentStatus.ONGOING);
        Long movie = register(RatingContentSubcategory.MOVIE, RatingContentStatus.ENDED);
        Long cafe = register(RatingContentSubcategory.CAFE, RatingContentStatus.ONGOING);
        Long hidden = register(RatingContentSubcategory.DRAMA, RatingContentStatus.ONGOING);
        ratingService.changeContentVisibility(hidden, false);

        assertThat(ids(new RatingContentQuery(null, null, null, 0, 20))).containsExactly(cafe, movie, drama);
        assertThat(ids(new RatingContentQuery(RatingContentCategory.MEDIA, null, null, 0, 20)))
                .containsExactly(movie, drama);
        assertThat(ids(new RatingContentQuery(null, RatingContentSubcategory.DRAMA, null, 0, 20)))
                .containsExactly(drama);
        assertThat(ids(new RatingContentQuery(null, null, RatingContentStatus.ONGOING, 0, 20)))
                .containsExactly(cafe, drama);
        assertThat(ids(new RatingContentQuery(RatingContentCategory.MEDIA, null, RatingContentStatus.ENDED, 0, 20)))
                .containsExactly(movie);
        assertThat(ids(new RatingContentQuery(RatingContentCategory.MEDIA, RatingContentSubcategory.MOVIE,
                RatingContentStatus.ONGOING, 0, 20))).isEmpty();
    }

    private java.util.List<Long> ids(RatingContentQuery query) {
        return ratingService.getContents(query).content().stream().map(RatingContentSummaryResponse::id).toList();
    }

    @Test
    @DisplayName("대분류와 맞지 않는 소분류로 거르면 RATING_CATEGORY_MISMATCH")
    void 카테고리_불일치() {
        assertError(() -> ratingService.getContents(new RatingContentQuery(
                        RatingContentCategory.FOOD, RatingContentSubcategory.DRAMA, null, 0, 20)),
                RatingErrorCode.RATING_CATEGORY_MISMATCH);
    }

    @Test
    @DisplayName("숨긴 콘텐츠 상세는 RATING_CONTENT_NOT_FOUND이고, 숨김을 풀면 다시 보인다")
    void 숨긴_콘텐츠() {
        Long contentId = register(RatingContentSubcategory.DRAMA, RatingContentStatus.ONGOING);
        ratingService.changeContentVisibility(contentId, false);

        assertError(() -> ratingService.getContent(contentId), RatingErrorCode.RATING_CONTENT_NOT_FOUND);

        ratingService.changeContentVisibility(contentId, true);
        assertThat(ratingService.getContent(contentId).id()).isEqualTo(contentId);
    }

    @Test
    @DisplayName("없는 콘텐츠를 조회하거나 숨기면 RATING_CONTENT_NOT_FOUND")
    void 없는_콘텐츠() {
        assertError(() -> ratingService.getContent(UNKNOWN_CONTENT), RatingErrorCode.RATING_CONTENT_NOT_FOUND);
        assertError(() -> ratingService.changeContentVisibility(UNKNOWN_CONTENT, false),
                RatingErrorCode.RATING_CONTENT_NOT_FOUND);
    }

    @Test
    @DisplayName("상세와 목록에 좋아요·싫어요 수, 좋아요 비율, 각각의 평균 별점을 담는다")
    void 호불호_집계() {
        Long contentId = register(RatingContentSubcategory.DRAMA, RatingContentStatus.ONGOING);
        Long noReview = register(RatingContentSubcategory.MOVIE, RatingContentStatus.ONGOING);
        review(contentId, 1L, Sentiment.LIKE, 5);
        review(contentId, 2L, Sentiment.LIKE, 4);
        review(contentId, 3L, Sentiment.DISLIKE, 1);

        SentimentStats stats = ratingService.getContent(contentId).stats();
        assertThat(stats.likeCount()).isEqualTo(2);
        assertThat(stats.dislikeCount()).isEqualTo(1);
        assertThat(stats.likeRatio()).as("2/3 = 66.7% → 67").isEqualTo(67);
        assertThat(stats.likeAverageScore()).isEqualTo(4.5);
        assertThat(stats.dislikeAverageScore()).isEqualTo(1.0);

        var page = ratingService.getContents(new RatingContentQuery(null, null, null, 0, 20));
        assertThat(page.content()).filteredOn(c -> c.id().equals(contentId)).singleElement()
                .satisfies(c -> assertThat(c.stats()).isEqualTo(stats));
        assertThat(page.content()).filteredOn(c -> c.id().equals(noReview)).singleElement()
                .satisfies(c -> {
                    assertThat(c.stats().reviewCount()).isZero();
                    assertThat(c.stats().likeRatio()).as("후기가 없으면 비율 없음").isNull();
                });
    }

    @Test
    @DisplayName("한쪽 후기만 있으면 다른 쪽 평균 별점은 null이다")
    void 한쪽_후기만() {
        Long contentId = register(RatingContentSubcategory.DRAMA, RatingContentStatus.ONGOING);
        review(contentId, 1L, Sentiment.LIKE, 3);

        SentimentStats stats = ratingService.getContent(contentId).stats();
        assertThat(stats.likeRatio()).isEqualTo(100);
        assertThat(stats.dislikeAverageScore()).isNull();
    }
}
