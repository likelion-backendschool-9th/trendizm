package com.trendizm.backend.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * created_at, updated_at을 자동으로 채운다. 두 컬럼이 있는 엔티티는 이 클래스를 상속한다.
 * 예: public class Member extends BaseTimeEntity
 * created_at만 있으면 BaseCreatedEntity를 상속한다.
 */
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity extends BaseCreatedEntity {

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
