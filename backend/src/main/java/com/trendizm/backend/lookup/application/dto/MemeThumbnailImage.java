package com.trendizm.backend.lookup.application.dto;

/** 저장한 썸네일 이미지. 컨트롤러가 그대로 응답 본문과 Content-Type으로 내려준다 */
public record MemeThumbnailImage(byte[] content, String contentType) {
}
