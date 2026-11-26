package com.trendizm.backend.accuse.application.port.out;

import com.trendizm.backend.accuse.domain.entity.Accuse;

/** 구현체는 adapter/out/persistence/JpaAccuseRepository (Spring Data가 자동 구현) */
public interface AccuseRepository {

    /** 바로 INSERT해서 (target, target_id, user_id) UNIQUE 위반을 이 자리에서 알 수 있게 한다 */
    Accuse saveAndFlush(Accuse accuse);

    boolean existsByTargetAndTargetIdAndUserId(String target, Long targetId, Long userId);
}
