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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * memes 테이블. 단어 하나당 문서 하나다(word UNIQUE).
 * 문서는 그 단어에 첫 의견을 쓸 때 함께 만들어진다(의견 작성 기능에서 호출).
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
     * 단어 비교용 표준 형태: 앞뒤 공백 제거.
     * 대소문자·띄어쓰기 차이까지 같은 단어로 볼지는 아직 정하지 않았다. 단어로 조회할 때도 이 값으로 찾는다.
     */
    public static String normalizeWord(String word) {
        return word.strip();
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
