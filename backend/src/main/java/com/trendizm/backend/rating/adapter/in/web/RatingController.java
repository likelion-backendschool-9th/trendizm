package com.trendizm.backend.rating.adapter.in.web;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.rating.application.dto.RatingContentQuery;
import com.trendizm.backend.rating.application.dto.RatingContentResponse;
import com.trendizm.backend.rating.application.dto.RatingContentSummaryResponse;
import com.trendizm.backend.rating.application.exception.RatingErrorCode;
import com.trendizm.backend.rating.application.port.RatingService;
import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 호불호 평점 API. 경로의 ratingId는 콘텐츠(rating_contents) id다.
 * 조회는 로그인 없이 허용한다 (auth/adapter/out/security/SecurityConfig).
 * 콘텐츠 등록·숨김은 관리자 API(/api/admin/ratings)에서 RatingService를 호출한다.
 */
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    /**
     * 콘텐츠 목록 (최신 등록순). category·subcategory·status는 slug 값(예: media, drama, ongoing)이고
     * 보내지 않거나 빈 값(?category=)이면 전체다.
     * 없는 slug면 RATING_INVALID_CATEGORY·RATING_INVALID_STATUS
     */
    @GetMapping
    public PageResponse<RatingContentSummaryResponse> getContents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String status,
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
        return ratingService.getContents(
                new RatingContentQuery(parsedCategory, parsedSubcategory, parsedStatus, page, size));
    }

    @GetMapping("/{ratingId}")
    public RatingContentResponse getContent(@PathVariable Long ratingId) {
        return ratingService.getContent(ratingId);
    }

    /** 화면에서 "전체"를 고르면 ?category= 처럼 빈 값이 올 수 있어서, 보내지 않은 것과 같게 본다 */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
