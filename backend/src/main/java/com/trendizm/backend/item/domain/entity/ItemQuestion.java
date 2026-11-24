package com.trendizm.backend.item.domain.entity;

import com.trendizm.backend.global.entity.BaseTimeEntity;
import com.trendizm.backend.item.domain.enums.ItemCategory;
import com.trendizm.backend.item.domain.enums.ItemQuestionStatus;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * item_questions 테이블. 트렌드 아이템 질문 글.
 * 대분류(category)는 소분류에서 정해지므로 create()에서 소분류로 채운다.
 * 관리자가 숨기면 status가 HIDDEN이 되고, 일반 조회(목록·상세·댓글·추천)에서 보이지 않는다.
 */
@Entity
@Table(name = "item_questions", indexes = @Index(columnList = "category, subcategory"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemQuestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 질문한 회원 id (users 테이블은 id 값만 저장한다) */
    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ItemSubcategory subcategory;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ItemQuestionStatus status;

    /**
     * 조회수. 상세를 볼 때마다 1씩 올린다(같은 사람이 다시 봐도 올라간다).
     * 이미 있는 행이 있는 DB(ddl-auto: update)에도 칼럼이 추가되도록 DDL 기본값을 0으로 둔다.
     */
    @ColumnDefault("0")
    @Column(nullable = false)
    private int viewCount;

    private ItemQuestion(Long userId, ItemSubcategory subcategory, String title, String content) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.subcategory = Objects.requireNonNull(subcategory, "subcategory");
        this.category = subcategory.getCategory();
        this.title = Objects.requireNonNull(title, "title");
        this.content = Objects.requireNonNull(content, "content");
        this.status = ItemQuestionStatus.PUBLIC;
    }

    public static ItemQuestion create(Long userId, ItemSubcategory subcategory, String title, String content) {
        return new ItemQuestion(userId, subcategory, title, content);
    }

    public boolean isPublic() {
        return status == ItemQuestionStatus.PUBLIC;
    }

    /** 상세 조회 시 조회수를 1 올린다. 동시에 여러 명이 보면 일부가 덮어써질 수 있지만 조회수라 허용한다 */
    public void increaseViewCount() {
        this.viewCount++;
    }

    /** 관리자 숨김·숨김 해제 */
    public void changeStatus(ItemQuestionStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }
}
