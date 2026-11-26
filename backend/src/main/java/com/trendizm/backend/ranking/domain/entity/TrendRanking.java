package com.trendizm.backend.ranking.domain.entity;

import com.trendizm.backend.ranking.domain.enums.Platform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * trend_rankings 테이블. 플랫폼·카테고리별 트렌드 순위 키워드 하나.
 * 한 번 수집한 순위들은 collectedAt이 같은 값이고, 이 값끼리 한 스냅샷이다. 한 스냅샷 안에서 순위(rank)는 겹치지 않는다.
 * 카테고리(trend_ranking_categories)는 엔티티를 참조하지 않고 id만 저장한다.
 */
@Entity
@Table(name = "trend_rankings",
        indexes = @Index(columnList = "platform, category_id, collected_at"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"platform", "category_id", "collected_at", "rank"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrendRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long categoryId;

    /** DB에는 상수 이름(소문자 slug)이 그대로 들어간다. 예: youtube */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Platform platform;

    @Column(nullable = false)
    private int rank;

    /** 순위 키워드 */
    @Column(nullable = false)
    private String keyword;

    /** AI 요약. 생성 전이거나 생성에 실패하면 null */
    @Column(columnDefinition = "text")
    private String summary;

    /** 집계 기간의 시작. collectedAt과의 차이로 며칠 동안의 순위인지(예: 최근 7일, 14일) 판단한다. 없을 수 있다 */
    private LocalDateTime startedAt;

    /** 수집을 마친 시각. 같은 값끼리 한 스냅샷이다 */
    @Column(nullable = false)
    private LocalDateTime collectedAt;

    private TrendRanking(Long categoryId, Platform platform, int rank, String keyword,
                         LocalDateTime startedAt, LocalDateTime collectedAt) {
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId");
        this.platform = Objects.requireNonNull(platform, "platform");
        this.rank = rank;
        this.keyword = Objects.requireNonNull(keyword, "keyword");
        this.startedAt = startedAt == null ? null : startedAt.truncatedTo(ChronoUnit.MICROS);
        this.collectedAt = Objects.requireNonNull(collectedAt, "collectedAt").truncatedTo(ChronoUnit.MICROS);
    }

    /**
     * 같은 스냅샷의 순위는 모두 같은 startedAt·collectedAt으로 만든다. 요약은 비어 있는 채로 만들고 changeSummary로 채운다.
     * 두 시각은 마이크로초까지만 남긴다. DB(timestamp)는 마이크로초까지 저장하므로, 나노초가 남아 있으면
     * 저장할 때 쓴 값으로 그 스냅샷을 다시 찾지 못한다 (global.config.JpaAuditingConfig와 같은 이유).
     */
    public static TrendRanking create(Long categoryId, Platform platform, int rank, String keyword,
                                      LocalDateTime startedAt, LocalDateTime collectedAt) {
        return new TrendRanking(categoryId, platform, rank, keyword, startedAt, collectedAt);
    }

    /** AI 요약을 저장한다. 다시 생성하면 덮어쓴다 */
    public void changeSummary(String summary) {
        this.summary = summary;
    }
}
