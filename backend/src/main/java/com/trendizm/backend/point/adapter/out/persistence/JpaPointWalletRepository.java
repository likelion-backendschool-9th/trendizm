package com.trendizm.backend.point.adapter.out.persistence;

import com.trendizm.backend.point.application.port.out.PointWalletRepository;
import com.trendizm.backend.point.domain.PointWallet;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** PointWalletRepository(out 포트)의 메서드를 Spring Data가 구현한다. 잠금 설정은 out 포트에 JPA를 노출하지 않으려고 여기에 둔다. */
public interface JpaPointWalletRepository extends JpaRepository<PointWallet, Long>, PointWalletRepository {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from PointWallet w where w.userId = :userId")
    Optional<PointWallet> findByUserIdForUpdate(@Param("userId") Long userId);
}
