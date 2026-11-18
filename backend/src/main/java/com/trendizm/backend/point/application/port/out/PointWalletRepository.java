package com.trendizm.backend.point.application.port.out;

import com.trendizm.backend.point.domain.PointWallet;
import java.util.Optional;

/** 구현체는 adapter/out/persistence/JpaPointWalletRepository (Spring Data가 자동 구현). */
public interface PointWalletRepository {

    /** 바로 DB에 반영한다. 같은 회원의 지갑을 동시에 만들면 여기서 DataIntegrityViolationException이 난다 */
    PointWallet saveAndFlush(PointWallet wallet);

    /** 잠그지 않고 읽는다 (잔액 조회용) */
    Optional<PointWallet> findByUserId(Long userId);

    /** 지갑 행을 잠그고 읽는다 (SELECT ... FOR UPDATE). 잔액을 바꿀 때 쓴다 */
    Optional<PointWallet> findByUserIdForUpdate(Long userId);
}
