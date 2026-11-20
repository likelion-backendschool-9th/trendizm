package com.trendizm.backend.lookup.adapter.in.web;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.MemeResponse;
import com.trendizm.backend.lookup.application.port.MemeService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 밈 문서 조회. 조회(GET)는 로그인 없이 허용한다 (auth/adapter/out/security/SecurityConfig).
 * 문서는 첫 의견을 쓸 때 함께 만들어지므로 등록 API는 따로 두지 않는다. 숨김은 관리자 API(12.x)에서 한다.
 */
@RestController
@RequestMapping("/api/lookup/memes")
@RequiredArgsConstructor
public class MemeController {

    private final MemeService memeService;

    /** 최근 등록된 문서 목록. keyword를 주면 단어에 keyword가 들어간 문서만 */
    @GetMapping
    public PageResponse<MemeResponse> getMemes(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        return memeService.getMemes(keyword, page, size);
    }

    /** 단어로 문서 하나를 조회한다. 예: GET /api/lookup/memes/알잘딱깔센 (URL 인코딩해서 보낸다) */
    @GetMapping("/{word}")
    public MemeResponse getMeme(@PathVariable String word) {
        return memeService.getMeme(word);
    }
}
