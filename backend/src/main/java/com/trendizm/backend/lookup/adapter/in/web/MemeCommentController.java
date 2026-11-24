package com.trendizm.backend.lookup.adapter.in.web;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.adapter.in.web.dto.CreateMemeCommentRequest;
import com.trendizm.backend.lookup.adapter.in.web.dto.UpdateMemeCommentRequest;
import com.trendizm.backend.lookup.application.dto.MemeCommentResponse;
import com.trendizm.backend.lookup.application.port.MemeCommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 밈 의견 댓글 API. 조회(GET)는 로그인 없이, 작성·수정·삭제·좋아요는 로그인이 필요하다 (auth/adapter/out/security/SecurityConfig).
 * 댓글은 의견 id로 다루고, 수정·삭제·좋아요는 댓글 id로 한다.
 */
@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
public class MemeCommentController {

    /** 비로그인 요청이면 principal이 문자열 "anonymousUser"라서 null로 바꾼다 */
    private static final String OPTIONAL_USER_ID = "#this == 'anonymousUser' ? null : id";

    private final MemeCommentService commentService;

    /** 댓글 목록 (최신순, 대댓글은 댓글마다 오래된 순). 로그인했으면 likedByMe가 들어간다 */
    @GetMapping("/interpretations/{interpretationId}/comments")
    public PageResponse<MemeCommentResponse> getComments(
            @PathVariable Long interpretationId,
            @AuthenticationPrincipal(expression = OPTIONAL_USER_ID) Long viewerId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        return commentService.getComments(interpretationId, viewerId, page, size);
    }

    /** 댓글 작성. parentId가 있으면 대댓글 */
    @PostMapping("/interpretations/{interpretationId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public MemeCommentResponse createComment(
            @PathVariable Long interpretationId,
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody CreateMemeCommentRequest request) {
        return commentService.createComment(request.toCommand(interpretationId, userId));
    }

    /** 내 댓글 수정 */
    @PatchMapping("/comments/{commentId}")
    public MemeCommentResponse updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody UpdateMemeCommentRequest request) {
        return commentService.updateComment(request.toCommand(commentId, userId));
    }

    /** 내 댓글 삭제. 대댓글이 남아 있으면 "삭제된 댓글"로 흔적만 남는다 */
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal(expression = "id") Long userId) {
        commentService.deleteComment(commentId, userId);
    }

    @PostMapping("/comments/{commentId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeComment(@PathVariable Long commentId, @AuthenticationPrincipal(expression = "id") Long userId) {
        commentService.likeComment(commentId, userId);
    }

    @DeleteMapping("/comments/{commentId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikeComment(@PathVariable Long commentId, @AuthenticationPrincipal(expression = "id") Long userId) {
        commentService.unlikeComment(commentId, userId);
    }
}
