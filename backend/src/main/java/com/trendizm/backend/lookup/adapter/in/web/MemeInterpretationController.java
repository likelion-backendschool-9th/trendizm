package com.trendizm.backend.lookup.adapter.in.web;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.adapter.in.web.dto.AddMemeInterpretationRequest;
import com.trendizm.backend.lookup.adapter.in.web.dto.CreateMemeInterpretationRequest;
import com.trendizm.backend.lookup.adapter.in.web.dto.VoteMemeInterpretationRequest;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;
import com.trendizm.backend.lookup.application.dto.MemeVoteResult;
import com.trendizm.backend.lookup.application.port.MemeInterpretationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 밈 의견 API. 조회(GET)는 로그인 없이, 작성·투표(POST·PUT·DELETE)는 로그인이 필요하다 (auth/adapter/out/security/SecurityConfig).
 * 문서 등록 API는 따로 없고, 새 단어·출처로 첫 의견을 쓰면 문서가 함께 만들어진다.
 * 단어는 경로에 넣지 않는다. 목록·작성은 문서 조회(GET /api/lookup/memes?q=)에서 고른 문서 id로 하고,
 * 목록에 없는 출처면 POST /api/lookup/interpretations에 단어·출처를 담아 쓴다.
 */
@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
public class MemeInterpretationController {

    /** 비로그인 요청이면 principal이 문자열 "anonymousUser"라서 null로 바꾼다 */
    private static final String OPTIONAL_USER_ID = "#this == 'anonymousUser' ? null : id";

    private final MemeInterpretationService interpretationService;

    /** 의견 목록 (신뢰도순). 로그인했으면 의견마다 내 투표(myVote)가 들어간다 */
    @GetMapping("/memes/{memeId}/interpretations")
    public PageResponse<MemeInterpretationResponse> getInterpretations(
            @PathVariable Long memeId,
            @AuthenticationPrincipal(expression = OPTIONAL_USER_ID) Long viewerId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        return interpretationService.getInterpretations(memeId, viewerId, page, size);
    }

    /** 고른 문서에 의견 작성 */
    @PostMapping("/memes/{memeId}/interpretations")
    @ResponseStatus(HttpStatus.CREATED)
    public MemeInterpretationResponse addInterpretation(
            @PathVariable Long memeId,
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody AddMemeInterpretationRequest request) {
        return interpretationService.addInterpretation(request.toCommand(memeId, userId));
    }

    /** 단어·출처로 의견 작성. 같은 단어·출처의 문서가 없으면 문서도 함께 만든다 */
    @PostMapping("/interpretations")
    @ResponseStatus(HttpStatus.CREATED)
    public MemeInterpretationResponse createInterpretation(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody CreateMemeInterpretationRequest request) {
        return interpretationService.createInterpretation(request.toCommand(userId));
    }

    /** 투표. 처음이면 저장, 다른 쪽이면 변경, 같은 쪽이면 그대로. 처리 후의 찬성·반대 수와 myVote를 돌려준다 */
    @PutMapping("/interpretations/{interpretationId}/vote")
    public MemeVoteResult vote(
            @PathVariable Long interpretationId,
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody VoteMemeInterpretationRequest request) {
        return interpretationService.vote(request.toCommand(interpretationId, userId));
    }

    /** 투표 취소. 투표하지 않았어도 성공이다 */
    @DeleteMapping("/interpretations/{interpretationId}/vote")
    public MemeVoteResult cancelVote(
            @PathVariable Long interpretationId,
            @AuthenticationPrincipal(expression = "id") Long userId) {
        return interpretationService.cancelVote(interpretationId, userId);
    }
}
