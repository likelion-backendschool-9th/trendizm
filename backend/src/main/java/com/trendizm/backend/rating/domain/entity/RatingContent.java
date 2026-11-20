package com.trendizm.backend.rating.domain.entity;

import com.trendizm.backend.global.entity.BaseCreatedEntity;
import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentExtraKey;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import com.trendizm.backend.rating.domain.enums.RatingContentVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * rating_contents 테이블. 평점을 매길 콘텐츠(영화·드라마·음식·장소 등). 관리자가 등록한다.
 * 대분류(category)는 소분류에서 정해지므로 create()에서 소분류로 채운다.
 * 진행 상태(status)와 관리자 숨김(visibility)은 별개다. 일반 조회는 visibility = PUBLIC만 보여 준다.
 */
@Entity
@Table(name = "rating_contents", indexes = @Index(columnList = "category, subcategory, status"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RatingContent extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RatingContentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RatingContentSubcategory subcategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RatingContentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RatingContentVisibility visibility;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 500)
    private String imageUrl;

    /** 관람 등급 (미디어·게임·웹툰 등). 해당 없으면 null */
    private Integer ageRating;

    /** 공개일 (방영·출시·발매·오픈일) */
    private LocalDate releasedAt;

    /** 소분류별 추가 정보. 키는 RatingContentExtraKey에 정한 것만 쓸 수 있다. 예: { "season": 2, "episodeLength": "60분" }. ERD jsonb */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> extra;

    private RatingContent(RatingContentSubcategory subcategory, RatingContentStatus status, String title,
                          String description, String imageUrl, Integer ageRating, LocalDate releasedAt,
                          Map<String, Object> extra) {
        this.subcategory = Objects.requireNonNull(subcategory, "subcategory");
        if (extra != null) {
            extra.keySet().stream()
                    .filter(key -> !RatingContentExtraKey.isAllowed(subcategory, key))
                    .findFirst()
                    .ifPresent(key -> {
                        throw new IllegalArgumentException(subcategory + "에 쓸 수 없는 추가 정보 키입니다. key=" + key);
                    });
        }
        this.category = subcategory.getCategory();
        this.status = Objects.requireNonNull(status, "status");
        this.visibility = RatingContentVisibility.PUBLIC;
        this.title = Objects.requireNonNull(title, "title");
        this.description = description;
        this.imageUrl = imageUrl;
        this.ageRating = ageRating;
        this.releasedAt = releasedAt;
        // Map.copyOf는 null 값을 받지 못해서(JSON에는 null이 올 수 있다) 복사본을 읽기 전용으로 감싼다
        this.extra = extra == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(extra));
    }

    /** 추가 정보에 이 소분류에서 쓸 수 없는 키가 있으면 IllegalArgumentException (RatingContentExtraKey 참고) */
    public static RatingContent create(RatingContentSubcategory subcategory, RatingContentStatus status, String title,
                                       String description, String imageUrl, Integer ageRating,
                                       LocalDate releasedAt, Map<String, Object> extra) {
        return new RatingContent(subcategory, status, title, description, imageUrl, ageRating, releasedAt, extra);
    }

    public boolean isPublic() {
        return visibility == RatingContentVisibility.PUBLIC;
    }

    /** 관리자 숨김·숨김 해제 */
    public void changeVisibility(RatingContentVisibility visibility) {
        this.visibility = Objects.requireNonNull(visibility, "visibility");
    }

    /** 진행 상태 변경 (예: 방영중 → 완결) */
    public void changeStatus(RatingContentStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    /** 대분류에 맞는 진행 상태 화면 이름. 예: 드라마 ONGOING → "방영중" */
    public String getStatusLabel() {
        return status.labelFor(category);
    }
}
