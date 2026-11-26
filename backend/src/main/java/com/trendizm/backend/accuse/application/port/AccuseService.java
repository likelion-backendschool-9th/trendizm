package com.trendizm.backend.accuse.application.port;

import com.trendizm.backend.accuse.application.dto.AccuseResponse;
import com.trendizm.backend.accuse.application.dto.CreateAccuseCommand;

/**
 * 신고. 다른 도메인이 자기 대상(밈 문서 등)이 있는지 확인한 뒤 호출한다. accuse는 다른 도메인을 부르지 않는다.
 */
public interface AccuseService {

    /**
     * 신고를 저장한다. 사유가 없는 값이면 ACCUSE_INVALID_REASON, 기타인데 내용이 비었으면 ACCUSE_DESCRIPTION_REQUIRED,
     * 내용이 500자를 넘으면 ACCUSE_DESCRIPTION_TOO_LONG, 같은 사람이 같은 대상을 이미 신고했으면(처리된 신고 포함)
     * ACCUSE_ALREADY_EXISTS. target이 없는 값이면 호출한 쪽의 잘못이라 IllegalArgumentException
     */
    AccuseResponse accuse(CreateAccuseCommand command);
}
