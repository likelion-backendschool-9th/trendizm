package com.trendizm.backend.ranking.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * trend_ranking_categories 테이블. 트렌드 순위 카테고리.
 * slug는 화면 주소(frontend/app/ranking/[platform]/[category])의 category 자리에 쓴다.
 * 순위 CSV 업로드는 CSV의 category 값(한글 이름)을 title로 찾는다.
 * 미분류(slug uncategorized)는 저장은 하지만 조회 응답(카테고리 목록·순위)에는 넣지 않는다.
 */
@Entity
@Table(name = "trend_ranking_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrendRankingCategory {

    /** 조회 응답에서 빼는 미분류 카테고리의 slug */
    public static final String UNCATEGORIZED_SLUG = "uncategorized";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** URL용 이름. 예: challenge, meme, trend, slang, food, pet, sports, travel, uncategorized */
    @Column(nullable = false, unique = true, length = 30)
    private String slug;

    /** 표시 이름. CSV의 category 값과 같다. 예: 챌린지, 밈, 미분류 */
    @Column(nullable = false, unique = true)
    private String title;

    private TrendRankingCategory(String slug, String title) {
        this.slug = Objects.requireNonNull(slug, "slug");
        this.title = Objects.requireNonNull(title, "title");
    }

    public static TrendRankingCategory create(String slug, String title) {
        return new TrendRankingCategory(slug, title);
    }

    /** 미분류면 true. 조회 응답에서 뺀다 */
    public boolean isUncategorized() {
        return UNCATEGORIZED_SLUG.equals(slug);
    }
}
