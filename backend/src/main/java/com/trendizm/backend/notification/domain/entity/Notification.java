package com.trendizm.backend.notification.domain.entity;

import com.trendizm.backend.global.entity.BaseTimeEntity;
import com.trendizm.backend.notification.domain.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * notifications 테이블. 받는 회원과 밈 문서·해석은 id만 저장한다(다른 도메인 테이블).
 * - 목록은 last_event_at 최신순이다. updated_at은 읽음 처리에도 바뀌어서, 정렬에 쓰면 방금 읽은 알림이 맨 위로 올라간다.
 * - subject는 알림을 만들 때의 밈 단어를 복사해 둔다. 문서가 숨겨지거나 지워져도 목록 문구가 깨지지 않게 한다.
 * - event_count는 MEME_VOTE면 그 해석의 동의 수, MEME_INTERPRETATION이면 1이다.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_receiver_read", columnList = "receiver_id, read_at"),
        @Index(name = "idx_notifications_receiver_last_event", columnList = "receiver_id, last_event_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    public static final int MAX_SUBJECT_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    /**
     * 알림 종류 이름(MEME_VOTE 등)을 문자열로 저장한다. @Enumerated나 변환기를 쓰면 Hibernate가 enum 값 목록으로
     * CHECK 제약을 만드는데, 운영 DB는 ddl-auto: update라 나중에 알림 종류를 추가해도 CHECK가 옛 목록 그대로 남아
     * 새 종류 저장이 실패한다. 문자열 칼럼이면 CHECK가 생기지 않는다. 밖에서는 getType()으로 enum을 쓴다.
     */
    @Getter(AccessLevel.NONE)
    @Column(nullable = false, length = 30)
    private String type;

    @Column(name = "meme_id", nullable = false)
    private Long memeId;

    @Column(name = "interpretation_id", nullable = false)
    private Long interpretationId;

    @Column(nullable = false, length = MAX_SUBJECT_LENGTH)
    private String subject;

    @Column(name = "event_count", nullable = false)
    private int eventCount;

    /** 읽은 시각. null이면 안 읽음 */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /** 알림이 생기거나 묶인 마지막 시각. 목록 정렬에 쓴다 */
    @Column(name = "last_event_at", nullable = false)
    private LocalDateTime lastEventAt;

    private Notification(Long receiverId, NotificationType type, Long memeId, Long interpretationId,
                         String subject, int eventCount, LocalDateTime occurredAt) {
        this.receiverId = receiverId;
        this.type = type.name();
        this.memeId = memeId;
        this.interpretationId = interpretationId;
        this.subject = subject;
        this.eventCount = eventCount;
        this.lastEventAt = occurredAt;
    }

    /** subject는 칼럼 길이에 맞게 자른다(밈 단어도 100자 제한이라 보통은 그대로다) */
    public static Notification create(Long receiverId, NotificationType type, Long memeId, Long interpretationId,
                                      String subject, int eventCount, LocalDateTime occurredAt) {
        return new Notification(receiverId, type, memeId, interpretationId,
                truncateSubject(subject), eventCount, occurredAt);
    }

    public static String truncateSubject(String subject) {
        return subject.length() > MAX_SUBJECT_LENGTH ? subject.substring(0, MAX_SUBJECT_LENGTH) : subject;
    }

    public NotificationType getType() {
        return NotificationType.valueOf(type);
    }

    public boolean isRead() {
        return readAt != null;
    }

    /** 읽음 처리. 이미 읽었으면 처음 읽은 시각을 그대로 두고 false */
    public boolean markAsRead(LocalDateTime now) {
        if (readAt != null) {
            return false;
        }
        readAt = now;
        return true;
    }
}
