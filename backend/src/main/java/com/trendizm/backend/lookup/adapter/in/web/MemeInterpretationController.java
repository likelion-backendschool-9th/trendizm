package com.trendizm.backend.lookup.adapter.in.web;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.adapter.in.web.dto.CreateMemeInterpretationRequest;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;
import com.trendizm.backend.lookup.application.port.MemeInterpretationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 밈 의견 API. 조회(GET)는 로그인 없이, 작성(POST)은 로그인이 필요하다 (auth/adapter/out/security/SecurityConfig).
 * 문서 등록 API는 따로 없고, 그 단어에 첫 의견을 쓰면 문서가 함께 만들어진다.
 * 단어는 경로에 넣지 않는다. 작성은 본문의 word로, 목록은 문서 조회(GET /api/lookup/memes?q=)로 받은 문서 id로 한다.
 */
@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
public class MemeInterpretationController {

    private final MemeInterpretationService interpretationService;

    /** 의견 목록 (신뢰도순) */
    @GetMapping("/memes/{memeId}/interpretations")
    public PageResponse<MemeInterpretationResponse> getInterpretations(
            @PathVariable Long memeId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        return interpretationService.getInterpretations(memeId, page, size);
    }

    /** 의견 작성. 문서가 없는 단어면 문서도 함께 만든다 */
    @PostMapping("/interpretations")
    @ResponseStatus(HttpStatus.CREATED)
    public MemeInterpretationResponse createInterpretation(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody CreateMemeInterpretationRequest request) {
        return interpretationService.createInterpretation(request.toCommand(userId));
    }
}
