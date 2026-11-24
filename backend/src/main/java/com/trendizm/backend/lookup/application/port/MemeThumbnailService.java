package com.trendizm.backend.lookup.application.port;

import com.trendizm.backend.lookup.application.dto.MemeThumbnailImage;
import java.util.Optional;

/**
 * 밈 문서 썸네일. 문서가 새로 만들어지면 커밋 뒤에 따로(비동기) 출처에서 이미지를 받아 저장하고 파일명을 기록한다.
 * 이미지 주소: 유튜브는 영상 ID 규칙(SourceUrl.youtubeThumbnail), 그 밖의 출처는 페이지 메타데이터(og:image).
 * 못 받으면 thumbnail은 null로 두고 화면에서 대체 이미지를 보여 준다.
 */
public interface MemeThumbnailService {

    /** 문서의 썸네일을 채운다. 이미 있거나, 문서가 없거나, 이미지를 못 받으면 아무것도 하지 않는다 */
    void fillThumbnail(Long memeId);

    /** 저장한 썸네일 이미지. 없거나 잘못된 파일명이면 empty */
    Optional<MemeThumbnailImage> getThumbnail(String filename);
}
