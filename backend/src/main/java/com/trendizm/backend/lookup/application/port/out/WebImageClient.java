package com.trendizm.backend.lookup.application.port.out;

import java.util.Optional;

/**
 * 출처 페이지에서 대표 이미지를 찾고 받아 온다. 구현체는 adapter/out/web/HttpWebImageClient.
 * 사용자가 넣은 주소에 서버가 접속하므로, 구현체는 내부망 주소를 막고 시간·크기를 제한한다.
 * 실패하면 예외 대신 empty를 준다(썸네일은 없어도 문서는 정상이다).
 */
public interface WebImageClient {

    /** 페이지의 메타데이터(og:image 등)에 적힌 대표 이미지 주소. 없거나 페이지를 못 받으면 empty */
    Optional<String> findImageUrl(String pageUrl);

    /** 이미지를 받는다. 허용한 형식(ThumbnailImageType)이 아니거나 너무 크거나 못 받으면 empty */
    Optional<DownloadedImage> download(String imageUrl);
}
