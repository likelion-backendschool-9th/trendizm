package com.trendizm.backend.rating.adapter.in.web.dto;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.rating.application.dto.SaveRatingReviewCommand;
import com.trendizm.backend.rating.application.exception.RatingErrorCode;
import com.trendizm.backend.rating.domain.enums.Sentiment;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 후기 작성·수정. sentiment는 like·dislike, progress(몇 회까지 봤는지)는 해당 없으면 비운다.
 * 예: { "sentiment": "like", "score": 5, "title": "최고", "body": "...", "progress": 8 }
 */
public record SaveRatingReviewRequest(
        @NotBlank(message = "좋아요/싫어요를 선택해 주세요.")
        String sentiment,

        @NotNull(message = "별점을 선택해 주세요.")
        @Min(value = 1, message = "별점은 1~5여야 합니다.")
        @Max(value = 5, message = "별점은 1~5여야 합니다.")
        Integer score,

        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용을 입력해 주세요.")
        @Size(max = 2000, message = "내용은 2000자 이하여야 합니다.")
        String body,

        @PositiveOrZero(message = "시청 정도는 0 이상이어야 합니다.")
        Integer progress
) {

    /** sentiment가 like·dislike가 아니면 RATING_INVALID_SENTIMENT */
    public SaveRatingReviewCommand toCommand(Long contentId, Long userId) {
        Sentiment parsed = Sentiment.fromSlug(sentiment.strip())
                .orElseThrow(() -> new BusinessException(RatingErrorCode.RATING_INVALID_SENTIMENT));
        return new SaveRatingReviewCommand(contentId, userId, parsed, score, title.strip(), body.strip(), progress);
    }
}
