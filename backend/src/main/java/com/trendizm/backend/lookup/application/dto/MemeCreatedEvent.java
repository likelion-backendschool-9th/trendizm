package com.trendizm.backend.lookup.application.dto;

/** 밈 문서가 새로 만들어졌다. 커밋된 뒤 썸네일을 채운다(MemeThumbnailServiceImpl) */
public record MemeCreatedEvent(Long memeId) {
}
