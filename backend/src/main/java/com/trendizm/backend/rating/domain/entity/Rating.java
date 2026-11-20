package com.trendizm.backend.rating.domain.entity;

import com.trendizm.backend.global.entity.BaseTimeEntity;
import com.trendizm.backend.rating.domain.enums.Sentiment;
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
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ratings 테이블. 콘텐츠에 남긴 후기(호/불호 + 별점 + 제목 + 본문). 한 회원은 콘텐츠당 후기를 하나만 쓴다.
 * API 경로에서는 "review"라고 부른다.
 */
@Entity
@Table(name = "ratings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"content_id", "user_id"}),
        indexes = @Index(columnList = "content_id, sentiment"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rating extends BaseTimeEntity {

    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long contentId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Sentiment sentiment;

    /** 별점 1~5 */
    @Column(nullable = false)
    private int score;

    /** 한줄 제목 */
    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    /** 몇 회까지 봤는지. 해당 없으면 null */
    private Integer progress;

    private Rating(Long contentId, Long userId, Sentiment sentiment, int score, String title, String body,
                   Integer progress) {
        this.contentId = Objects.requireNonNull(contentId, "contentId");
        this.userId = Objects.requireNonNull(userId, "userId");
        apply(sentiment, score, title, body, progress);
    }

    /** 별점이 1~5가 아니거나 progress가 음수면 IllegalArgumentException */
    public static Rating create(Long contentId, Long userId, Sentiment sentiment, int score, String title,
                                String body, Integer progress) {
        return new Rating(contentId, userId, sentiment, score, title, body, progress);
    }

    /** 후기 수정. 검사는 create와 같다 */
    public void update(Sentiment sentiment, int score, String title, String body, Integer progress) {
        apply(sentiment, score, title, body, progress);
    }

    private void apply(Sentiment sentiment, int score, String title, String body, Integer progress) {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException("별점은 1~5여야 합니다. score=" + score);
        }
        if (progress != null && progress < 0) {
            throw new IllegalArgumentException("progress는 0 이상이어야 합니다. progress=" + progress);
        }
        this.sentiment = Objects.requireNonNull(sentiment, "sentiment");
        this.score = score;
        this.title = Objects.requireNonNull(title, "title");
        this.body = Objects.requireNonNull(body, "body");
        this.progress = progress;
    }
}
