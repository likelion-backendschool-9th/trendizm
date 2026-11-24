package com.trendizm.backend.rating.adapter.in.web;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.rating.adapter.in.web.dto.SaveRatingReviewRequest;
import com.trendizm.backend.rating.application.dto.RatingContentQuery;
import com.trendizm.backend.rating.application.dto.RatingContentResponse;
import com.trendizm.backend.rating.application.dto.RatingContentSummaryResponse;
import com.trendizm.backend.rating.application.dto.RatingReviewQuery;
import com.trendizm.backend.rating.application.dto.RatingReviewResponse;
import com.trendizm.backend.rating.application.dto.RatingReviewSaveResult;
import com.trendizm.backend.rating.application.exception.RatingErrorCode;
import com.trendizm.backend.rating.application.port.RatingContentSort;
import com.trendizm.backend.rating.application.port.RatingService;
import com.trendizm.backend.rating.application.port.ReviewSort;
import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import com.trendizm.backend.rating.domain.enums.Sentiment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 호불호 평점 API. 경로의 ratingId는 콘텐츠(rating_contents) id다.
 * 조회(GET)는 로그인 없이, 후기 작성·공감(POST·DELETE)은 로그인이 필요하다 (auth/adapter/out/security/SecurityConfig).
 * 콘텐츠 등록·숨김은 관리자 API(/api/admin/ratings)에서 RatingService를 호출한다.
 */
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    /**
     * 로그인하지 않고 공개 API를 부르면 principal이 "anonymousUser" 문자열이라 expression = "id"가 실패한다.
     * 그래서 조회 API는 이 식으로 로그인하지 않았을 때 null을 받는다.
     */
    private static final String OPTIONAL_USER_ID = "#this == 'anonymousUser' ? null : id";

    private final RatingService ratingService;

    /**
     * 콘텐츠 목록. category·subcategory·status는 slug 값(예: media, drama, ongoing)이고
     * 보내지 않거나 빈 값(?category=)이면 전체다.
     * 없는 slug면 RATING_INVALID_CATEGORY·RATING_INVALID_STATUS.
     * sort는 latest(기본, 최신 등록순)·reviews(후기 많은 순)·likes(좋아요 많은 순)·dislikes(싫어요 많은 순)·score(평균 별점 순),
     * 없는 값이면 RATING_INVALID_SORT. 콘텐츠마다 좋아요·싫어요 후기를 공감 많은 순 2개씩 미리보기로 담는다
     */
    @GetMapping
    public PageResponse<RatingContentSummaryResponse> getContents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        RatingContentCategory parsedCategory = isBlank(category) ? null
                : RatingContentCategory.fromSlug(category.strip())
                        .orElseThrow(() -> new BusinessException(RatingErrorCode.RATING_INVALID_CATEGORY));
        RatingContentSubcategory parsedSubcategory = isBlank(subcategory) ? null
                : RatingContentSubcategory.fromSlug(subcategory.strip())
                        .orElseThrow(() -> new BusinessException(RatingErrorCode.RATING_INVALID_CATEGORY));
        RatingContentStatus parsedStatus = isBlank(status) ? null
                : RatingContentStatus.fromSlug(status.strip())
                        .orElseThrow(() -> new BusinessException(RatingErrorCode.RATING_INVALID_STATUS));
        RatingContentSort parsedSort = RatingContentSort.fromSlug(sort.strip())
                .orElseThrow(() -> new BusinessException(RatingErrorCode.RATING_INVALID_SORT));
        return ratingService.getContents(
                new RatingContentQuery(parsedCategory, parsedSubcategory, parsedStatus, parsedSort, page, size));
    }

    @GetMapping("/{ratingId}")
    public RatingContentResponse getContent(@PathVariable Long ratingId) {
        return ratingService.getContent(ratingId);
    }

    /**
     * 후기 목록. sentiment(like·dislike)는 보내지 않거나 빈 값이면 전체,
     * sort는 latest(기본)·score-high·score-low·likes(공감 많은 순).
     * 없는 값이면 RATING_INVALID_SENTIMENT·RATING_INVALID_SORT
     */
    @GetMapping("/{ratingId}/reviews")
    public PageResponse<RatingReviewResponse> getReviews(
            @PathVariable Long ratingId,
            @AuthenticationPrincipal(expression = OPTIONAL_USER_ID) Long viewerId,
            @RequestParam(required = false) String sentiment,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        Sentiment parsedSentiment = isBlank(sentiment) ? null : Sentiment.fromSlug(sentiment.strip())
                .orElseThrow(() -> new BusinessException(RatingErrorCode.RATING_INVALID_SENTIMENT));
        ReviewSort parsedSort = ReviewSort.fromSlug(sort)
                .orElseThrow(() -> new BusinessException(RatingErrorCode.RATING_INVALID_SORT));
        return ratingService.getReviews(
                new RatingReviewQuery(ratingId, parsedSentiment, parsedSort, viewerId, page, size));
    }

    /**
     * 후기 작성·수정. 이미 쓴 후기가 있으면 수정한다. 새로 만들면 201, 수정하면 200.
     * 처음 작성하면 rewardPoint에 적립한 포인트(10P)가 들어 있다.
     */
    @PostMapping("/{ratingId}/reviews")
    public ResponseEntity<RatingReviewSaveResult> saveReview(
            @PathVariable Long ratingId,
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody SaveRatingReviewRequest request) {
        RatingReviewSaveResult result = ratingService.saveReview(request.toCommand(ratingId, userId));
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(result);
    }

    @PostMapping("/reviews/{reviewId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeReview(@PathVariable Long reviewId, @AuthenticationPrincipal(expression = "id") Long userId) {
        ratingService.likeReview(reviewId, userId);
    }

    @DeleteMapping("/reviews/{reviewId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikeReview(@PathVariable Long reviewId, @AuthenticationPrincipal(expression = "id") Long userId) {
        ratingService.unlikeReview(reviewId, userId);
    }

    /** 화면에서 "전체"를 고르면 ?category= 처럼 빈 값이 올 수 있어서, 보내지 않은 것과 같게 본다 */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
