package com.trendizm.backend.lookup.domain.entity;

import com.trendizm.backend.global.entity.BaseTimeEntity;
import com.trendizm.backend.lookup.domain.enums.MemeVoteType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * meme_interpretation_votes 테이블. 한 사람이 한 의견에 한 번만 투표한다((interpretation_id, user_id) UNIQUE).
 * 동의·비동의를 바꾸면 type만 수정한다. 찬성·반대 수는 meme_interpretations의 집계 칼럼에 함께 반영한다.
 * 저장·변경·삭제는 동시 요청에도 수가 틀어지지 않게 JpaMemeInterpretationVoteRepository의 쿼리로 한다.
 */
@Entity
@Table(name = "meme_interpretation_votes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_meme_interpretation_votes_interpretation_user", columnNames = {"interpretation_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemeInterpretationVote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 투표한 의견 id (meme_interpretations.id) */
    @Column(name = "interpretation_id", nullable = false)
    private Long interpretationId;

    /** 투표한 회원 id (users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemeVoteType type;

    private MemeInterpretationVote(Long interpretationId, Long userId, MemeVoteType type) {
        this.interpretationId = interpretationId;
        this.userId = userId;
        this.type = type;
    }

    /** 테스트 데이터용. 서비스는 insertIfAbsent로 저장한다 */
    public static MemeInterpretationVote create(Long interpretationId, Long userId, MemeVoteType type) {
        return new MemeInterpretationVote(interpretationId, userId, type);
    }
}
