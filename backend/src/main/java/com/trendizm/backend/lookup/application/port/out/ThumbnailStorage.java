package com.trendizm.backend.lookup.application.port.out;

import com.trendizm.backend.lookup.domain.enums.ThumbnailImageType;
import java.util.Optional;

/** 썸네일 이미지 파일 저장소. 구현체는 adapter/out/storage/LocalThumbnailStorage (서버 디스크) */
public interface ThumbnailStorage {

    /** 저장하고 새 파일명(임의 이름 + 확장자)을 돌려준다 */
    String save(byte[] content, ThumbnailImageType type);

    /** 파일명으로 읽는다. 형식에 맞지 않는 이름(../ 등)이거나 없으면 empty */
    Optional<DownloadedImage> load(String filename);
}
