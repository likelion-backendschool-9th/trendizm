package com.trendizm.backend.point.application.port.out;

import com.trendizm.backend.point.domain.PointWallet;
import java.util.Optional;

/** 구현체는 adapter/out/persistence/JpaPointWalletRepository (Spring Data가 자동 구현). */
public interface PointWalletRepository {

    /**
     * 회원의 지갑이 없으면 잔액 0으로 만들고, 이미 있으면 아무것도 하지 않는다 (INSERT ... ON CONFLICT DO NOTHING).
     * 같은 회원의 첫 지갑을 동시에 만들어도 실패하지 않는다.
     *
     * @return 새로 만들었으면 1, 이미 있었으면 0
     */
    int insertIfAbsent(Long userId);

    /** 잠그지 않고 읽는다 (잔액 조회용) */
    Optional<PointWallet> findByUserId(Long userId);

    /** 지갑 행을 잠그고 읽는다 (SELECT ... FOR UPDATE). 잔액을 바꿀 때 쓴다 */
    Optional<PointWallet> findByUserIdForUpdate(Long userId);
}
