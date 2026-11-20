package com.trendizm.backend.lookup.domain.entity;

import com.trendizm.backend.global.entity.BaseCreatedEntity;
import com.trendizm.backend.lookup.domain.enums.MemeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * memes 테이블. 단어 하나당 문서 하나다(word UNIQUE).
 * 문서는 그 단어에 첫 의견을 쓸 때 함께 만들어진다(MemeInterpretationServiceImpl).
 * 출처는 대표 출처 하나만 둔다(source_url, title, thumbnail_url). 없을 수 있다.
 * 작성자는 다른 도메인(member) 엔티티를 참조하지 않고 id만 저장한다.
 */
@Entity
@Table(name = "memes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meme extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String word;

    /** 대표 출처 주소 (피드·동영상·게시물 링크) */
    @Column(length = 500)
    private String sourceUrl;

    /** 대표 출처의 제목 */
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

    /** 단어는 normalizeWord로 바꿔 저장한다. 출처 값은 없으면 null */
    public static Meme create(String word, Long userId, String sourceUrl, String title, String thumbnailUrl) {
        return new Meme(normalizeWord(word), userId, sourceUrl, title, thumbnailUrl);
    }

    /**
     * 단어 저장·조회·검색에 공통으로 쓰는 형태: 앞뒤 공백을 지우고 소문자로 바꾼다.
     * - 대소문자: 소문자로 저장해서 TMI와 tmi는 같은 문서다. word UNIQUE가 DB에서 중복을 막는다.
     * - 띄어쓰기: 다른 단어로 본다("알잘딱 깔센"과 "알잘딱깔센"은 다른 문서).
     * Locale.ROOT: 실행 환경 언어 설정(예: 터키어)에 따라 i·I 변환이 달라지지 않게 한다.
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
