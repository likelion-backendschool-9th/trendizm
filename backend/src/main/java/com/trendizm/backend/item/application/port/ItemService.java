package com.trendizm.backend.item.application.port;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.item.application.dto.CreateItemCommand;
import com.trendizm.backend.item.application.dto.ItemAdjacentQuestions;
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

    /** 질문 글 상세. 볼 때마다 조회수를 1 올린다 */
    ItemQuestionResponse getQuestion(Long questionId, Long viewerId);

    /** 이전/다음 글 (공개 글 기준). 글이 없거나 숨겨졌으면 ITEM_QUESTION_NOT_FOUND */
    ItemAdjacentQuestions getAdjacentQuestions(Long questionId);

    ItemQuestionResponse createQuestion(CreateItemQuestionCommand command);

    /**
     * 관리자 숨김·숨김 해제. 관리자 API(PATCH /api/admin/items/{id}/visibility)에서 호출한다.
     * 숨긴 글도 찾으며, 없으면 ITEM_QUESTION_NOT_FOUND
     */
    void changeQuestionVisibility(Long questionId, boolean visible);

    /** 댓글 목록 (최신순). 댓글마다 대댓글을 오래된 순으로 함께 담는다. page·size는 댓글(대댓글 제외) 기준 */
    default PageResponse<ItemCommentResponse> getComments(Long questionId, Long viewerId, int page, int size) {
        return getComments(questionId, viewerId, ItemCommentSort.LATEST, page, size);
    }

    /** 댓글 목록. sort는 최신순 또는 공감 많은 순이고, 대댓글은 정렬과 상관없이 오래된 순이다 */
    PageResponse<ItemCommentResponse> getComments(Long questionId, Long viewerId, ItemCommentSort sort,
                                                  int page, int size);

    /** 대댓글은 같은 글의 댓글에만 달 수 있다(대댓글에 답글 불가). 아니면 ITEM_INVALID_PARENT_COMMENT */
    ItemCommentResponse createComment(CreateItemCommentCommand command);

    /** 이미 공감했으면 아무것도 하지 않는다 */
    void likeComment(Long commentId, Long userId);

    /** 공감하지 않았으면 아무것도 하지 않는다 */
    void unlikeComment(Long commentId, Long userId);

    /** 추천 아이템 목록 (등록순). 보는 사람이 등록했거나 열람한 아이템은 열린 상태, 나머지는 잠긴 상태다 */
    List<ItemResponse> getRecommendations(Long questionId, Long viewerId);

    ItemResponse createRecommendation(CreateItemCommand command);

    /**
     * 추천 아이템 잠금 해제. 열람자에게서 열람 가격(100P)을 차감하고 등록자에게 같은 금액을 적립한 뒤, 열린 아이템을 돌려준다.
     * 이미 열람했거나 내가 등록한 아이템이면 포인트를 쓰지 않고 열린 아이템을 돌려준다.
     * 없거나 숨긴 질문 글의 아이템이면 ITEM_RECOMMENDATION_NOT_FOUND, 포인트가 부족하면 포인트 도메인의 INSUFFICIENT_POINT_BALANCE
     */
    ItemResponse unlockRecommendation(Long recommendationId, Long userId);
}
