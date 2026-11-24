package com.trendizm.backend.lookup.application.port.out;

import com.trendizm.backend.lookup.domain.enums.ThumbnailImageType;

/** 받아 온 이미지. 형식은 Content-Type과 파일 앞부분으로 확인한 값이다 */
public record DownloadedImage(byte[] content, ThumbnailImageType type) {
}
