package com.trendizm.backend.lookup.domain.entity;

import com.trendizm.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * meme_comments 테이블. 의견의 댓글과 대댓글.
 * parentId가 비어 있으면 댓글, 값이 있으면 그 댓글에 단 대댓글이다. 대댓글에는 다시 답글을 달 수 없다(한 단계만).
 * deletedAt은 대댓글이 남아 있는 댓글을 지웠을 때만 채운다(흔적만 남김). 대댓글이 없으면 행을 지운다.
 */
@Entity
@Table(name = "meme_comments", indexes = {
        @Index(name = "idx_meme_comments_interpretation_parent", columnList = "interpretation_id, parent_id"),
        @Index(name = "idx_meme_comments_parent", columnList = "parent_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemeComment extends BaseTimeEntity {

    public static final int MAX_CONTENT_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interpretation_id", nullable = false)
    private Long interpretationId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = MAX_CONTENT_LENGTH)
    private String content;

    /** 비어 있으면 정상, 값이 있으면 삭제된 댓글(대댓글이 남아 있어 흔적만 표시) */
    private LocalDateTime deletedAt;

    private MemeComment(Long interpretationId, Long parentId, Long userId, String content) {
        this.interpretationId = Objects.requireNonNull(interpretationId, "interpretationId");
        this.parentId = parentId;
        this.userId = Objects.requireNonNull(userId, "userId");
        this.content = Objects.requireNonNull(content, "content");
    }

    /** parentId가 null이면 댓글, 아니면 대댓글 */
    public static MemeComment create(Long interpretationId, Long parentId, Long userId, String content) {
        return new MemeComment(interpretationId, parentId, userId, content);
    }

    public boolean isReply() {
        return parentId != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isWrittenBy(Long userId) {
        return this.userId.equals(userId);
    }

    public void edit(String content) {
        this.content = Objects.requireNonNull(content, "content");
    }

    /** 대댓글이 남아 있는 댓글을 지울 때 쓴다. 내용은 응답에 내보내지 않는다 */
    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}
