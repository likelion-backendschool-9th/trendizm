package com.trendizm.backend.item.domain.entity;

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

/**
 * item_comment_likes 테이블. 댓글(대댓글 포함) 공감. 한 회원이 한 댓글에 한 번만 공감한다.
 * 서비스에서는 동시에 눌러도 한 줄만 생기도록 ItemCommentLikeRepository.insertIfAbsent로 만든다 (create는 테스트·직접 생성용).
 */
@Entity
@Table(name = "item_comment_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemCommentLike extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long commentId;

    @Column(nullable = false)
    private Long userId;

    private ItemCommentLike(Long commentId, Long userId) {
        this.commentId = Objects.requireNonNull(commentId, "commentId");
        this.userId = Objects.requireNonNull(userId, "userId");
    }

    public static ItemCommentLike create(Long commentId, Long userId) {
        return new ItemCommentLike(commentId, userId);
    }
}
