package com.trendizm.backend.lookup.adapter.in.web;

import com.trendizm.backend.lookup.adapter.in.web.dto.MemeRequest;
import com.trendizm.backend.lookup.application.dto.MemeResponse;
import com.trendizm.backend.lookup.application.port.MemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 없이 모든 요청 허용 (auth/adapter/out/security/SecurityConfig) */
@RestController
@RequestMapping("/api/lookup/memes")
@RequiredArgsConstructor
public class MemeController {

    private final MemeService memeService;

    @GetMapping
    public Page<MemeResponse> getMemes(Pageable pageable) {
        return memeService.getMemes(pageable);
    }

    @GetMapping("/{memeId}")
    public MemeResponse getMeme(@PathVariable Long memeId) {
        return memeService.getMeme(memeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createMeme(@Valid @RequestBody MemeRequest request) {
        return memeService.createMeme();
    }

    @PutMapping("/{memeId}")
    public void updateMeme(@PathVariable Long memeId, @Valid @RequestBody MemeRequest request) {
        memeService.updateMeme(memeId);
    }

    @DeleteMapping("/{memeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMeme(@PathVariable Long memeId) {
        memeService.deleteMeme(memeId);
    }
}
