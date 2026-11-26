package com.trendizm.backend.accuse.domain.entity;

import com.trendizm.backend.accuse.domain.enums.AccuseReason;
import com.trendizm.backend.global.entity.BaseCreatedEntity;
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
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 신고. 밈·평점·아이템 등 여러 도메인이 같이 쓰는 공용 테이블이라 대상은 target(대상 테이블 이름) + target_id로 가리킨다(FK 없음).
 * target은 enum이 아니라 각 도메인 엔티티의 테이블 이름 상수(예: Meme.TABLE_NAME = "memes")다. 대상이 늘어도 accuse는 바뀌지 않고,
 * 값 목록 CHECK 제약도 생기지 않는다.
 * 대상이 있는지는 신고를 받는 도메인이 확인한 뒤 저장한다. 한 사람은 같은 대상을 한 번만 신고할 수 있다.
 * closed_at이 null이면 처리 대기 중이고, 관리자가 처리하면 처리 시각을 채운다(행은 지우지 않는다).
 */
@Entity
@Table(name = "accuses",
        uniqueConstraints = @UniqueConstraint(name = "uk_accuses_target_user",
                columnNames = {"target", "target_id", "user_id"}),
        indexes = @Index(name = "idx_accuses_target_closed", columnList = "target, closed_at, target_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Accuse extends BaseCreatedEntity {

    /** target 칼럼 길이 */
    public static final int MAX_TARGET_LENGTH = 30;

    /** description 칼럼 길이 */
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 신고 대상 테이블 이름. 예: "memes" */
    @Column(nullable = false, length = MAX_TARGET_LENGTH)
    private String target;

    /** 신고 대상 id. target이 memes면 memes.id */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /** 신고한 회원 id (users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccuseReason reason;

    /** 신고 내용. 사유가 기타면 필수, 그 외에는 없으면 null */
    @Column(length = MAX_DESCRIPTION_LENGTH)
    private String description;

    /** 관리자가 처리한 시각. null이면 처리 대기 중 */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    private Accuse(String target, Long targetId, Long userId, AccuseReason reason, String description) {
        this.target = target;
        this.targetId = targetId;
        this.userId = userId;
        this.reason = reason;
        this.description = description;
    }

    /** 내용 검사(기타면 필수 등)는 서비스에서 한다. 여기서는 빈 내용을 null로만 바꾼다 */
    public static Accuse create(String target, Long targetId, Long userId, AccuseReason reason,
                                String description) {
        if (target == null || target.isBlank() || target.length() > MAX_TARGET_LENGTH) {
            throw new IllegalArgumentException("신고 대상 테이블 이름은 1~30자여야 합니다: " + target);
        }
        Objects.requireNonNull(targetId, "신고 대상 id는 필수입니다");
        Objects.requireNonNull(userId, "신고한 회원 id는 필수입니다");
        Objects.requireNonNull(reason, "신고 사유는 필수입니다");
        return new Accuse(target, targetId, userId, reason, normalizeDescription(description));
    }

    /** 앞뒤 공백을 지우고, 비어 있으면 null */
    public static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String stripped = description.strip();
        return stripped.isEmpty() ? null : stripped;
    }

    /** 처리 대기 중인지 */
    public boolean isOpen() {
        return closedAt == null;
    }

    /** 관리자가 처리했다. 이미 처리됐으면 처음 처리한 시각을 그대로 둔다 */
    public void close(LocalDateTime now) {
        if (isOpen()) {
            this.closedAt = Objects.requireNonNull(now, "처리 시각은 필수입니다");
        }
    }
}
