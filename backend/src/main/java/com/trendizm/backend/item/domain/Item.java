package com.trendizm.backend.item.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * items 테이블. 컬럼은 아직 정해지지 않아 id만 있다.
 * 컬럼이 정해지면 필드를 추가하고 create()에 파라미터를 더한다.
 */
@Entity
@Table(name = "items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public static Item create() {
        return new Item();
    }
}
