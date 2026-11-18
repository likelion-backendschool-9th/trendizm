package com.trendizm.backend.point.adapter.out.persistence;

import com.trendizm.backend.point.application.port.out.PointWalletRepository;
import com.trendizm.backend.point.domain.PointWallet;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** PointWalletRepository(out 포트)의 메서드를 Spring Data가 구현한다. 쿼리·잠금 설정은 out 포트에 JPA를 노출하지 않으려고 여기에 둔다. */
public interface JpaPointWalletRepository extends JpaRepository<PointWallet, Long>, PointWalletRepository {

    @Override
    @Modifying
    @Query(value = """
            INSERT INTO point_wallets (user_id, balance, version, created_at, updated_at)
            VALUES (:userId, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    // 충돌 대상(user_id)을 적지 않는다: 테스트용 H2의 PostgreSQL 모드가 대상 없는 형태만 지원한다.
    // point_wallets의 unique 제약은 user_id(와 PK)뿐이라 PostgreSQL에서도 같은 의미다.
    int insertIfAbsent(@Param("userId") Long userId);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from PointWallet w where w.userId = :userId")
    Optional<PointWallet> findByUserIdForUpdate(@Param("userId") Long userId);
}
