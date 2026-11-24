package com.trendizm.backend.ranking.domain.entity;

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
 * trend_ranking_links 테이블. 순위 키워드의 관련 링크(영상·게시물 등).
 * 조회 API는 순위마다 링크를 전부 sortOrder 순으로 주고, 한 번에 몇 개씩 보여 줄지(더보기)는 화면에서 정한다.
 * 순위(trend_rankings)는 엔티티를 참조하지 않고 id만 저장한다.
 */
@Entity
@Table(name = "trend_ranking_links", indexes = @Index(columnList = "ranking_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrendRankingLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long rankingId;

    /** 제목 최대 길이 (칼럼 varchar(255)) */
    public static final int TITLE_MAX_LENGTH = 255;

    /** 주소 최대 길이 (칼럼 varchar(500)) */
    public static final int URL_MAX_LENGTH = 500;

    /** 게시물 제목(캡션 등). 255자를 넘으면 잘라서 저장하고, 비어 있으면 null */
    @Column(length = TITLE_MAX_LENGTH)
    private String title;

    @Column(nullable = false, length = URL_MAX_LENGTH)
    private String url;

    /** 플랫폼 정책상 표시할 수 없으면 null */
    @Column(length = 500)
    private String thumbnail;

    @Column(length = 500)
    private String description;

    /** 게시일. 화면에 보여 주는 날짜 */
    private LocalDateTime publishedAt;

    /** 표시 순서 (0부터) */
    @Column(nullable = false)
    private int sortOrder;

    private TrendRankingLink(Long rankingId, String title, String url, String thumbnail, String description,
                             LocalDateTime publishedAt, int sortOrder) {
        this.rankingId = Objects.requireNonNull(rankingId, "rankingId");
        this.title = normalizeTitle(title);
        this.url = Objects.requireNonNull(url, "url");
        this.thumbnail = thumbnail;
        this.description = description;
        this.publishedAt = publishedAt;
        this.sortOrder = sortOrder;
    }

    public static TrendRankingLink create(Long rankingId, String title, String url, String thumbnail,
                                          String description, LocalDateTime publishedAt, int sortOrder) {
        return new TrendRankingLink(rankingId, title, url, thumbnail, description, publishedAt, sortOrder);
    }

    /**
     * 앞뒤 공백을 지우고, 비어 있으면 null, 255자를 넘으면 앞 255자만 남긴다.
     * 글자 수는 DB(varchar)처럼 코드 포인트로 센다. 이모지처럼 자바 문자 2개인 글자가 중간에서 잘리지 않는다.
     */
    static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        String stripped = title.strip();
        if (stripped.codePointCount(0, stripped.length()) <= TITLE_MAX_LENGTH) {
            return stripped;
        }
        return stripped.substring(0, stripped.offsetByCodePoints(0, TITLE_MAX_LENGTH));
    }
}
