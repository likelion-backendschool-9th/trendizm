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
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

/**
 * memes 테이블. 단어 + 출처 하나당 문서 하나다((word, source_url) UNIQUE).
 * 같은 단어라도 출처마다 쓰임이 다를 수 있어서, 출처가 다르면 문서를 따로 둔다. 출처는 필수다.
 * 문서는 그 단어·출처로 첫 의견을 쓸 때 함께 만들어진다(MemeInterpretationServiceImpl).
 * 작성자는 다른 도메인(member) 엔티티를 참조하지 않고 id만 저장한다.
 * DynamicUpdate: 변경 감지로 저장할 때 바뀐 칼럼만 UPDATE한다. 비동기로 요약·썸네일을 저장하는 사이
 * 다른 요청이 바꾼 칼럼(숨김 상태 등)을 읽어 둔 옛 값으로 덮어쓰지 않게 한다.
 */
@Entity
@DynamicUpdate
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

    /**
     * 단어에서 지우는 보이지 않는 문자: 제어 문자(줄바꿈·탭 등), 서식 문자(폭 없는 공백 U+200B, BOM 등),
     * 한글 채움 문자(ㅤ U+3164 등, 투명 닉네임에 자주 쓰임), 점자 빈칸(U+2800)
     */
    private static final Pattern INVISIBLE = Pattern.compile("[\\p{Cc}\\p{Cf}\\u115F\\u1160\\u3164\\uFFA0\\u2800]");

    /** source_url 칼럼 길이. SourceUrl.normalize로 바꾼 뒤의 길이를 이 값으로 확인한다 */
    public static final int MAX_SOURCE_URL_LENGTH = 500;

    @Column(nullable = false, length = MAX_WORD_LENGTH)
    private String word;

    /** 출처 주소 (동영상·게시물 링크). SourceUrl.normalize로 바꿔 저장한다 */
    @Column(name = "source_url", nullable = false, length = MAX_SOURCE_URL_LENGTH)
    private String sourceUrl;

    /** 출처의 제목 */
    private String title;

    /**
     * 저장한 썸네일 이미지 파일명 (예: 3f2c...e1.jpg). 주소가 아니라 파일명이고, 화면은 GET /api/lookup/memes/thumbnails/{파일명}으로 불러온다.
     * 문서를 만든 뒤 출처에서 이미지를 받아 저장하면 채운다(MemeThumbnailServiceImpl). 못 받았으면 null이고 화면에서 대체 이미지를 보여 준다.
     */
    @Column(length = 500)
    private String thumbnail;

    /**
     * 의견 AI 요약. 신뢰도(동의 - 비동의) 상위 의견을 요약한 글이다(MemeSummaryServiceImpl).
     * 의견이 2개 이상일 때 만들고, 그 전에는 null이다. 화면에는 "AI 요약"으로 표시하고 원래 의견도 함께 보여 준다.
     */
    @Column(columnDefinition = "text")
    private String aiSummary;

    /** 요약을 만든 시각. 같은 문서의 요약을 너무 자주 다시 만들지 않게 간격을 잴 때 쓴다 */
    private LocalDateTime aiSummaryUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemeStatus status;

    /** 문서를 처음 만든 회원 id (users.id) */
    @Column(nullable = false)
    private Long userId;

    private Meme(String word, Long userId, String sourceUrl, String title) {
        this.word = word;
        this.userId = userId;
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.status = MemeStatus.PUBLIC;
    }

    /**
     * 단어는 normalizeWord, 출처 주소는 SourceUrl.normalize로 바꿔 저장한다. 출처 주소는 필수이고 제목은 없으면 null.
     * 썸네일은 비워 두고, 문서가 저장된 뒤 출처에서 이미지를 받아 changeThumbnail로 채운다.
     */
    public static Meme create(String word, Long userId, String sourceUrl, String title) {
        Objects.requireNonNull(sourceUrl, "출처 주소는 필수입니다");
        return new Meme(normalizeWord(word), userId, SourceUrl.normalize(sourceUrl), title);
    }

    /** 저장한 썸네일 이미지 파일명을 기록한다 */
    public void changeThumbnail(String filename) {
        this.thumbnail = Objects.requireNonNull(filename, "filename");
    }

    /** 새로 만든 AI 요약과 만든 시각을 기록한다 */
    public void changeAiSummary(String summary, LocalDateTime now) {
        this.aiSummary = Objects.requireNonNull(summary, "summary");
        this.aiSummaryUpdatedAt = Objects.requireNonNull(now, "now");
    }

    /** 마지막 요약을 만든 지 interval이 안 지났으면 true (다시 만들지 않는다) */
    public boolean isAiSummaryFresh(LocalDateTime now, Duration interval) {
        return aiSummaryUpdatedAt != null && aiSummaryUpdatedAt.isAfter(now.minus(interval));
    }

    /**
     * 단어 저장·조회·검색에 공통으로 쓰는 형태. 눈으로 같아 보이는 단어가 다른 문서가 되지 않게 맞춘다.
     * 1. 유니코드 NFKC 정규화: 자모가 분리된 한글(NFD)을 합치고, 전각 문자(ＴＭＩ)를 일반 문자로 바꾼다
     *    (①→1, x²→x2처럼 모양을 단순하게 바꾸는 것도 포함된다)
     * 2. 보이지 않는 문자(INVISIBLE)를 지운다. 단어 중간의 일반 공백은 남긴다
     * 3. 앞뒤 공백을 지우고 소문자로 바꾼다
     * 보이지 않는 문자만 있던 단어는 빈 문자열이 된다. 저장 전에 서비스에서 MEME_WORD_BLANK로 막는다.
     * - 대소문자: 소문자로 저장해서 TMI와 tmi는 같은 단어다. (word, source_url) UNIQUE가 DB에서 중복을 막는다.
     * - 띄어쓰기: 다른 단어로 본다("알잘딱 깔센"과 "알잘딱깔센"은 다른 문서).
     * Locale.ROOT: 실행 환경 언어 설정(예: 터키어)에 따라 i·I 변환이 달라지지 않게 한다.
     * 소문자로 바꾸면 길어지는 글자가 있다(예: "İ" → "i̇" 2글자). 저장 전에는 바꾼 뒤의 길이를 MAX_WORD_LENGTH로 확인한다.
     */
    public static String normalizeWord(String word) {
        String composed = Normalizer.normalize(word, Normalizer.Form.NFKC);
        return INVISIBLE.matcher(composed).replaceAll("").strip().toLowerCase(Locale.ROOT);
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
