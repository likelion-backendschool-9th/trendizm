package com.trendizm.backend.lookup.adapter.in.web;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.MemeResponse;
import com.trendizm.backend.lookup.application.port.MemeService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 밈 문서 조회. 조회(GET)는 로그인 없이 허용한다 (auth/adapter/out/security/SecurityConfig).
 * 단어는 경로가 아니라 쿼리 파라미터로 받는다. 단어에 '/'나 '%'가 들어가면 경로에서는 Spring Security가 400으로 막는다.
 * 문서는 첫 의견을 쓸 때 함께 만들어지므로 등록 API는 따로 두지 않는다. 숨김은 관리자 API(12.x)에서 한다.
 */
@RestController
@RequestMapping("/api/lookup/memes")
@RequiredArgsConstructor
public class MemeController {

    private final MemeService memeService;

    /** 최근 등록된 문서 목록. keyword를 주면 단어에 keyword가 들어간 문서만. q가 있으면 getMeme이 처리한다 */
    @GetMapping(params = "!q")
    public PageResponse<MemeResponse> getMemes(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        return memeService.getMemes(keyword, page, size);
    }

    /** 단어가 정확히 같은 문서 하나를 조회한다. 예: GET /api/lookup/memes?q=알잘딱깔센 (URL 인코딩해서 보낸다) */
    @GetMapping(params = "q")
    public MemeResponse getMeme(
            @RequestParam
            @NotBlank(message = "단어를 입력해 주세요.")
            @Size(max = 100, message = "단어는 100자 이하여야 합니다.") String q) {
        return memeService.getMeme(q);
    }
}
