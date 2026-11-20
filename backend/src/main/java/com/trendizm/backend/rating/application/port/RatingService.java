package com.trendizm.backend.rating.application.port;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.rating.application.dto.RatingContentQuery;
import com.trendizm.backend.rating.application.dto.RatingContentResponse;
import com.trendizm.backend.rating.application.dto.RatingContentSummaryResponse;
import com.trendizm.backend.rating.application.dto.RegisterRatingContentCommand;

/**
 * 호불호 평점 유스케이스 (in 포트). 지금은 콘텐츠 등록·숨김·조회를 다루고, 후기·후기 공감은 다음 작업에서 추가한다.
 * 관리자가 숨긴 콘텐츠(HIDDEN)는 일반 조회에서 없는 콘텐츠로 취급한다(RATING_CONTENT_NOT_FOUND).
 */
public interface RatingService {

    /** 공개 콘텐츠 목록 (최신 등록순). 콘텐츠마다 호/불호 집계를 담는다 */
    PageResponse<RatingContentSummaryResponse> getContents(RatingContentQuery query);

    /** 공개 콘텐츠 상세. 호/불호 수·비율과 각각의 평균 별점을 담는다 */
    RatingContentResponse getContent(Long contentId);

    /** 관리자 콘텐츠 등록. 관리자 API(POST /api/admin/ratings)에서 호출한다 */
    RatingContentResponse registerContent(RegisterRatingContentCommand command);

    /**
     * 관리자 숨김·숨김 해제. 관리자 API에서 호출한다. 진행 상태(status)와는 별개다.
     * 숨긴 콘텐츠도 찾으며, 없으면 RATING_CONTENT_NOT_FOUND
     */
    void changeContentVisibility(Long contentId, boolean visible);
}
