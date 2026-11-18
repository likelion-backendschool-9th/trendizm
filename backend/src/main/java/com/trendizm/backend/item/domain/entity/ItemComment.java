package com.trendizm.backend.item.domain.entity;

import com.trendizm.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * item_comments 테이블. 질문 글의 댓글과 대댓글.
 * parentId가 비어 있으면 댓글, 값이 있으면 그 댓글에 단 대댓글이다. 대댓글에는 다시 답글을 달 수 없다(한 단계만).
 */
@Entity
@Table(name = "item_comments", indexes = {
        @Index(columnList = "question_id, parent_id"),
        @Index(columnList = "parent_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    private Long parentId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500)
    private String content;

    private ItemComment(Long questionId, Long parentId, Long userId, String content) {
        this.questionId = Objects.requireNonNull(questionId, "questionId");
        this.parentId = parentId;
        this.userId = Objects.requireNonNull(userId, "userId");
        this.content = Objects.requireNonNull(content, "content");
    }

    /** parentId가 null이면 댓글, 아니면 대댓글 */
    public static ItemComment create(Long questionId, Long parentId, Long userId, String content) {
        return new ItemComment(questionId, parentId, userId, content);
    }

    public boolean isReply() {
        return parentId != null;
    }
}
