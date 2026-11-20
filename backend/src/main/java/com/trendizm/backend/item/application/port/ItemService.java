package com.trendizm.backend.item.application.port;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.item.application.dto.CreateItemCommand;
import com.trendizm.backend.item.application.dto.CreateItemCommentCommand;
import com.trendizm.backend.item.application.dto.CreateItemQuestionCommand;
import com.trendizm.backend.item.application.dto.ItemCommentResponse;
import com.trendizm.backend.item.application.dto.ItemQuestionQuery;
import com.trendizm.backend.item.application.dto.ItemQuestionResponse;
import com.trendizm.backend.item.application.dto.ItemQuestionSummaryResponse;
import com.trendizm.backend.item.application.dto.ItemResponse;
import java.util.List;

/**
 * 트렌드 아이템 유스케이스 (in 포트). 질문 글·댓글·댓글 공감·추천 아이템을 다룬다.
 * 관리자가 숨긴 질문 글(HIDDEN)은 일반 조회와 쓰기에서 없는 글로 취급한다(ITEM_QUESTION_NOT_FOUND).
 * viewerId는 보는 사람의 회원 id이고, 로그인하지 않았으면 null이다.
 */
public interface ItemService {

    /** 공개 질문 글 목록 (최신순) */
    PageResponse<ItemQuestionSummaryResponse> getQuestions(ItemQuestionQuery query);

    ItemQuestionResponse getQuestion(Long questionId, Long viewerId);

    ItemQuestionResponse createQuestion(CreateItemQuestionCommand command);

    /**
     * 관리자 숨김·숨김 해제. 관리자 API(PATCH /api/admin/items/{id}/visibility)에서 호출한다.
     * 숨긴 글도 찾으며, 없으면 ITEM_QUESTION_NOT_FOUND
     */
    void changeQuestionVisibility(Long questionId, boolean visible);

    /** 댓글 목록 (최신순). 댓글마다 대댓글을 오래된 순으로 함께 담는다. page·size는 댓글(대댓글 제외) 기준 */
    PageResponse<ItemCommentResponse> getComments(Long questionId, Long viewerId, int page, int size);

    /** 대댓글은 같은 글의 댓글에만 달 수 있다(대댓글에 답글 불가). 아니면 ITEM_INVALID_PARENT_COMMENT */
    ItemCommentResponse createComment(CreateItemCommentCommand command);

    /** 이미 공감했으면 아무것도 하지 않는다 */
    void likeComment(Long commentId, Long userId);

    /** 공감하지 않았으면 아무것도 하지 않는다 */
    void unlikeComment(Long commentId, Long userId);

    /** 추천 아이템 목록 (등록순). 등록자 본인 것만 열린 상태이고 나머지는 잠긴 상태다 */
    List<ItemResponse> getRecommendations(Long questionId, Long viewerId);

    ItemResponse createRecommendation(CreateItemCommand command);
}
