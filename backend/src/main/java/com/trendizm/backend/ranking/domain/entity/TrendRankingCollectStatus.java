package com.trendizm.backend.ranking.domain.entity;

import com.trendizm.backend.ranking.domain.enums.Platform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * trend_ranking_collect_statuses 테이블. 플랫폼별 순위 수집 설정.
 * status가 true면 24시간을 times번으로 나눈 간격마다 한 번씩 수집하고(예: times가 2면 12시간마다), false면 수집하지 않는다.
 * 처음 만들면 수집하지 않는 상태(false)다.
 */
@Entity
@Table(name = "trend_ranking_collect_statuses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrendRankingCollectStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** DB에는 상수 이름(소문자 slug)이 그대로 들어간다. 예: youtube */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Platform platform;

    /** 한 번 수집할 때 받아오는 개수 */
    private Integer count;

    /** 수집할 때 받아올 목록의 정렬 기준 */
    @Column(length = 100)
    private String sortOrder;

    /** 24시간 동안 수집하는 횟수 */
    private Integer times;

    /** 수집 여부 */
    @Column(nullable = false)
    private boolean status;

    private TrendRankingCollectStatus(Platform platform, Integer count, String sortOrder, Integer times) {
        this.platform = Objects.requireNonNull(platform, "platform");
        this.count = count;
        this.sortOrder = sortOrder;
        this.times = times;
        this.status = false;
    }

    public static TrendRankingCollectStatus create(Platform platform, Integer count, String sortOrder, Integer times) {
        return new TrendRankingCollectStatus(platform, count, sortOrder, times);
    }

    /** 수집을 켜거나 끈다 */
    public void changeStatus(boolean status) {
        this.status = status;
    }
}
