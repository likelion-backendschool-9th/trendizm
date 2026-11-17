package com.trendizm.backend.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * created_at을 자동으로 채운다. created_at만 있는 테이블(items, point_histories 등)의 엔티티가 상속한다.
 * updated_at도 있으면 BaseTimeEntity를 상속한다.
 * 동작하려면 JpaAuditingConfig(@EnableJpaAuditing)가 있어야 한다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseCreatedEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
