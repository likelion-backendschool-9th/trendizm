package com.trendizm.backend.lookup.domain.entity;

import com.trendizm.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * meme_interpretations 테이블. 회원이 직접 쓰는 밈 의견(뜻 한 줄 + 본문)이다. 사용 예시는 본문(content)에 함께 쓴다.
 * 찬성·반대 수는 목록을 신뢰도순(찬성 - 반대)으로 정렬하려고 여기에 모아 둔다. 투표 기능에서 늘리고 줄인다.
 * 작성자는 다른 도메인(member) 엔티티를 참조하지 않고 id만 저장한다.
 */
@Entity
@Table(name = "meme_interpretations", indexes = @Index(name = "idx_meme_interpretations_meme_id", columnList = "memeId"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemeInterpretation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 의견이 달린 문서 id (memes.id) */
    @Column(nullable = false)
    private Long memeId;

    /** 작성한 회원 id (users.id) */
    @Column(nullable = false)
    private Long userId;

    /** 뜻 한 줄 요약 */
    @Column(nullable = false, length = 100)
    private String meaning;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private int agreeCount;

    @Column(nullable = false)
    private int disagreeCount;

    private MemeInterpretation(Long memeId, Long userId, String meaning, String content) {
        this.memeId = memeId;
        this.userId = userId;
        this.meaning = meaning;
        this.content = content;
    }

    /** 찬성·반대 수는 0에서 시작한다 */
    public static MemeInterpretation create(Long memeId, Long userId, String meaning, String content) {
        return new MemeInterpretation(memeId, userId, meaning, content);
    }
}
