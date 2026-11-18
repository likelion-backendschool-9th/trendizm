package com.trendizm.backend.item.adapter.in.web;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.item.adapter.in.web.dto.CreateItemCommentRequest;
import com.trendizm.backend.item.adapter.in.web.dto.CreateItemQuestionRequest;
import com.trendizm.backend.item.adapter.in.web.dto.CreateItemRequest;
import com.trendizm.backend.item.application.dto.ItemCommentResponse;
import com.trendizm.backend.item.application.dto.ItemQuestionQuery;
import com.trendizm.backend.item.application.dto.ItemQuestionResponse;
import com.trendizm.backend.item.application.dto.ItemQuestionSummaryResponse;
import com.trendizm.backend.item.application.dto.ItemResponse;
import com.trendizm.backend.item.application.port.ItemService;
import com.trendizm.backend.item.domain.enums.ItemCategory;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
 * 트렌드 아이템 API. 경로의 itemId는 질문 글 id다.
 * 조회(GET)는 로그인 없이, 쓰기(POST·DELETE)는 로그인이 필요하다 (auth/adapter/out/security/SecurityConfig).
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    /**
     * 로그인하지 않고 공개 API를 부르면 principal이 "anonymousUser" 문자열이라 expression = "id"가 실패한다.
     * 그래서 조회 API는 이 식으로 로그인하지 않았을 때 null을 받는다.
     */
    private static final String OPTIONAL_USER_ID = "#this == 'anonymousUser' ? null : id";

    private final ItemService itemService;

    /** 질문 글 목록 (최신순). category·subcategory는 code 값(예: FASHION, CLOTHING)이고 비우면 전체다 */
    @GetMapping
    public PageResponse<ItemQuestionSummaryResponse> getQuestions(
            @RequestParam(required = false) ItemCategory category,
            @RequestParam(required = false) ItemSubcategory subcategory,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        return itemService.getQuestions(new ItemQuestionQuery(category, subcategory, page, size));
    }

    @GetMapping("/{itemId}")
    public ItemQuestionResponse getQuestion(
            @PathVariable Long itemId,
            @AuthenticationPrincipal(expression = OPTIONAL_USER_ID) Long viewerId) {
        return itemService.getQuestion(itemId, viewerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemQuestionResponse createQuestion(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody CreateItemQuestionRequest request) {
        return itemService.createQuestion(request.toCommand(userId));
    }

    /** 댓글 목록 (최신순). 댓글마다 대댓글(replies)이 오래된 순으로 들어 있고, page·size는 댓글 기준이다 */
    @GetMapping("/{itemId}/comments")
    public PageResponse<ItemCommentResponse> getComments(
            @PathVariable Long itemId,
            @AuthenticationPrincipal(expression = OPTIONAL_USER_ID) Long viewerId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        return itemService.getComments(itemId, viewerId, page, size);
    }

    @PostMapping("/{itemId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemCommentResponse createComment(
            @PathVariable Long itemId,
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody CreateItemCommentRequest request) {
        return itemService.createComment(request.toCommand(itemId, userId));
    }

    @PostMapping("/comments/{commentId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeComment(@PathVariable Long commentId, @AuthenticationPrincipal(expression = "id") Long userId) {
        itemService.likeComment(commentId, userId);
    }

    @DeleteMapping("/comments/{commentId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikeComment(@PathVariable Long commentId, @AuthenticationPrincipal(expression = "id") Long userId) {
        itemService.unlikeComment(commentId, userId);
    }

    /** 추천 아이템 목록 (등록순). 잠긴 아이템은 이름·판매처·설명·이미지·링크가 null이다 */
    @GetMapping("/{itemId}/recommendations")
    public List<ItemResponse> getRecommendations(
            @PathVariable Long itemId,
            @AuthenticationPrincipal(expression = OPTIONAL_USER_ID) Long viewerId) {
        return itemService.getRecommendations(itemId, viewerId);
    }

    @PostMapping("/{itemId}/recommendations")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse createRecommendation(
            @PathVariable Long itemId,
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody CreateItemRequest request) {
        return itemService.createRecommendation(request.toCommand(itemId, userId));
    }
}
