package com.trendizm.backend.lookup.adapter.in.web;

import com.trendizm.backend.lookup.application.port.MemeThumbnailService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 저장한 밈 문서 썸네일 이미지. 문서 응답의 thumbnail(파일명)으로 부른다. 예: GET /api/lookup/memes/thumbnails/3f2c...e1.jpg
 * 조회(GET)라 로그인 없이 허용한다(SecurityConfig의 /api/lookup/**). 없거나 잘못된 파일명이면 404이고 화면은 대체 이미지를 보여 준다.
 * 파일명은 한 번 정해지면 내용이 바뀌지 않아서 오래 캐시해도 된다.
 */
@RestController
@RequestMapping("/api/lookup/memes/thumbnails")
@RequiredArgsConstructor
public class MemeThumbnailController {

    private final MemeThumbnailService thumbnailService;

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable String filename) {
        return thumbnailService.getThumbnail(filename)
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.contentType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                        .header("X-Content-Type-Options", "nosniff")
                        .body(image.content()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
