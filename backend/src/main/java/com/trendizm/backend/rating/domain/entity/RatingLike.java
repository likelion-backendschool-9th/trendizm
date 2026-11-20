package com.trendizm.backend.rating.domain.entity;

import com.trendizm.backend.global.entity.BaseCreatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** rating_likes 테이블. 후기 공감. 한 회원이 한 후기에 한 번만 공감한다 */
@Entity
@Table(name = "rating_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"rating_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RatingLike extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ratingId;

    @Column(nullable = false)
    private Long userId;

    private RatingLike(Long ratingId, Long userId) {
        this.ratingId = Objects.requireNonNull(ratingId, "ratingId");
        this.userId = Objects.requireNonNull(userId, "userId");
    }

    public static RatingLike create(Long ratingId, Long userId) {
        return new RatingLike(ratingId, userId);
    }
}
