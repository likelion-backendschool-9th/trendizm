package com.trendizm.backend.item.domain.entity;

import com.trendizm.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * items 테이블. 질문 글에 등록한 추천 아이템.
 * 댓글과 따로 등록한다(댓글로 추천하면 내용이 바로 공개되므로). 잠금 해제는 아이템 하나 단위이고 가격은 unlockPrice다.
 * 등록자 본인에게는 처음부터 열린 상태로 보여 준다.
 */
@Entity
@Table(name = "items", indexes = @Index(columnList = "question_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseTimeEntity {

    /** 열람 가격 기본값 (ERD items.unlock_price default 100) */
    public static final int DEFAULT_UNLOCK_PRICE = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    /** 추천 아이템 등록자 (열람될 때마다 포인트 적립) */
    @Column(nullable = false)
    private Long userId;

    /** 아이템 이름 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 판매처 */
    @Column(length = 100)
    private String seller;

    /** 가격 (원) */
    private Integer price;

    /** 예: ["화이트", "폭신한 재질"] (ERD jsonb) */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> tags;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(length = 500)
    private String imageUrl;

    /** 판매처 상품 페이지 */
    @Column(length = 500)
    private String linkUrl;

    /** 등록자 프로필에서 복사 (예: 20대). 프로필을 입력하지 않았으면 null. 회원 사용자화 정보(3.5)가 생기면 채운다 */
    @Column(length = 10)
    private String targetAgeGroup;

    /** 등록자 프로필에서 복사. 프로필을 입력하지 않았으면 null */
    @Column(length = 10)
    private String targetGender;

    @Column(nullable = false)
    private int unlockPrice;

    private Item(Long questionId, Long userId, String title, String seller, Integer price, List<String> tags,
                 String description, String imageUrl, String linkUrl) {
        this.questionId = Objects.requireNonNull(questionId, "questionId");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.title = Objects.requireNonNull(title, "title");
        this.seller = seller;
        this.price = price;
        this.tags = tags == null ? List.of() : List.copyOf(tags);
        this.description = Objects.requireNonNull(description, "description");
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.unlockPrice = DEFAULT_UNLOCK_PRICE;
    }

    public static Item create(Long questionId, Long userId, String title, String seller, Integer price,
                              List<String> tags, String description, String imageUrl, String linkUrl) {
        return new Item(questionId, userId, title, seller, price, tags, description, imageUrl, linkUrl);
    }

    public boolean isOwnedBy(Long memberId) {
        return memberId != null && memberId.equals(userId);
    }
}
