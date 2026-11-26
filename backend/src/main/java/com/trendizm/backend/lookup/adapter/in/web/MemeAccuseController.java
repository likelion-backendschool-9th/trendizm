package com.trendizm.backend.lookup.adapter.in.web;

import com.trendizm.backend.accuse.application.dto.AccuseResponse;
import com.trendizm.backend.lookup.adapter.in.web.dto.CreateMemeAccuseRequest;
import com.trendizm.backend.lookup.application.port.MemeAccuseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 밈 문서 신고 API. 쓰기 요청이라 로그인이 필요하다 (auth/adapter/out/security/SecurityConfig).
 * 한 사람은 같은 문서를 한 번만 신고할 수 있다.
 */
@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
public class MemeAccuseController {

    private final MemeAccuseService memeAccuseService;

    @PostMapping("/memes/{memeId}/accuses")
    @ResponseStatus(HttpStatus.CREATED)
    public AccuseResponse accuseMeme(
            @PathVariable Long memeId,
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody CreateMemeAccuseRequest request) {
        return memeAccuseService.accuseMeme(request.toCommand(memeId, userId));
    }
}
