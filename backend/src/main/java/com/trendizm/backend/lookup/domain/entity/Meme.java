package com.trendizm.backend.lookup.domain.entity;

import com.trendizm.backend.global.entity.BaseCreatedEntity;
import com.trendizm.backend.lookup.domain.enums.MemeStatus;
import com.trendizm.backend.lookup.domain.vo.SourceUrl;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Locale;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * memes 테이블. 단어 + 출처 하나당 문서 하나다((word, source_url) UNIQUE).
 * 같은 단어라도 출처마다 쓰임이 다를 수 있어서, 출처가 다르면 문서를 따로 둔다. 출처는 필수다.
 * 문서는 그 단어·출처로 첫 의견을 쓸 때 함께 만들어진다(MemeInterpretationServiceImpl).
 * 작성자는 다른 도메인(member) 엔티티를 참조하지 않고 id만 저장한다.
 */
@Entity
@Table(name = "memes",
        uniqueConstraints = @UniqueConstraint(name = "uk_memes_word_source_url", columnNames = {"word", "source_url"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meme extends BaseCreatedEntity {

    /** word 칼럼 길이. normalizeWord로 바꾼 뒤의 길이를 이 값으로 확인한다 */
    public static final int MAX_WORD_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** source_url 칼럼 길이. SourceUrl.normalize로 바꾼 뒤의 길이를 이 값으로 확인한다 */
    public static final int MAX_SOURCE_URL_LENGTH = 500;

    @Column(nullable = false, length = MAX_WORD_LENGTH)
    private String word;

    /** 출처 주소 (동영상·게시물 링크). SourceUrl.normalize로 바꿔 저장한다 */
    @Column(name = "source_url", nullable = false, length = MAX_SOURCE_URL_LENGTH)
    private String sourceUrl;

    /** 출처의 제목 */
    private String title;

    @Column(length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemeStatus status;

    /** 문서를 처음 만든 회원 id (users.id) */
    @Column(nullable = false)
    private Long userId;

    private Meme(String word, Long userId, String sourceUrl, String title, String thumbnailUrl) {
        this.word = word;
        this.userId = userId;
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.status = MemeStatus.PUBLIC;
    }

    /** 단어는 normalizeWord, 출처 주소는 SourceUrl.normalize로 바꿔 저장한다. 출처 주소는 필수이고 제목·썸네일은 없으면 null */
    public static Meme create(String word, Long userId, String sourceUrl, String title, String thumbnailUrl) {
        Objects.requireNonNull(sourceUrl, "출처 주소는 필수입니다");
        return new Meme(normalizeWord(word), userId, SourceUrl.normalize(sourceUrl), title, thumbnailUrl);
    }

    /**
     * 단어 저장·조회·검색에 공통으로 쓰는 형태: 앞뒤 공백을 지우고 소문자로 바꾼다.
     * - 대소문자: 소문자로 저장해서 TMI와 tmi는 같은 단어다. (word, source_url) UNIQUE가 DB에서 중복을 막는다.
     * - 띄어쓰기: 다른 단어로 본다("알잘딱 깔센"과 "알잘딱깔센"은 다른 문서).
     * Locale.ROOT: 실행 환경 언어 설정(예: 터키어)에 따라 i·I 변환이 달라지지 않게 한다.
     * 소문자로 바꾸면 길어지는 글자가 있다(예: "İ" → "i̇" 2글자). 저장 전에는 바꾼 뒤의 길이를 MAX_WORD_LENGTH로 확인한다.
     */
    public static String normalizeWord(String word) {
        return word.strip().toLowerCase(Locale.ROOT);
    }

    /** 관리자 숨김. 숨김·해제 API는 관리자 기능(12.x)에서 만든다 */
    public void hide() {
        this.status = MemeStatus.HIDDEN;
    }

    public void show() {
        this.status = MemeStatus.PUBLIC;
    }

    public boolean isHidden() {
        return status == MemeStatus.HIDDEN;
    }
}
