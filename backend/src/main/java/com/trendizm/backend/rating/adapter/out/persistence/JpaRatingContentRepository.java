package com.trendizm.backend.rating.adapter.out.persistence;

import com.trendizm.backend.rating.application.port.out.RatingContentRepository;
import com.trendizm.backend.rating.domain.entity.RatingContent;
import com.trendizm.backend.rating.domain.enums.RatingContentCategory;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import com.trendizm.backend.rating.domain.enums.RatingContentVisibility;
import com.trendizm.backend.rating.domain.enums.Sentiment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** RatingContentRepository(out 포트)의 메서드를 Spring Data가 구현한다 */
public interface JpaRatingContentRepository extends JpaRepository<RatingContent, Long>, RatingContentRepository {

    /** 집계 정렬 조회가 함께 쓰는 조건(공개 콘텐츠, 비어 있는 조건은 뺀다). 개수 조회에도 그대로 쓴다 */
    String PUBLIC_FILTER = """
            from RatingContent c
            where c.visibility = :visibility
              and (:category is null or c.category = :category)
              and (:subcategory is null or c.subcategory = :subcategory)
              and (:status is null or c.status = :status)
            """;

    @Override
    @Query(value = "select c " + PUBLIC_FILTER
            + " order by (select count(r) from Rating r where r.contentId = c.id) desc, c.id desc",
            countQuery = "select count(c) " + PUBLIC_FILTER)
    Page<RatingContent> findByVisibilityOrderByReviewCount(@Param("visibility") RatingContentVisibility visibility,
            @Param("category") RatingContentCategory category, @Param("subcategory") RatingContentSubcategory subcategory,
            @Param("status") RatingContentStatus status, Pageable pageable);

    @Override
    @Query(value = "select c " + PUBLIC_FILTER
            + " order by (select count(r) from Rating r where r.contentId = c.id and r.sentiment = :sentiment) desc,"
            + " c.id desc",
            countQuery = "select count(c) " + PUBLIC_FILTER)
    Page<RatingContent> findByVisibilityOrderBySentimentCount(@Param("visibility") RatingContentVisibility visibility,
            @Param("category") RatingContentCategory category, @Param("subcategory") RatingContentSubcategory subcategory,
            @Param("status") RatingContentStatus status, @Param("sentiment") Sentiment sentiment, Pageable pageable);

    @Override
    @Query(value = "select c " + PUBLIC_FILTER
            // 후기가 없으면 평균이 null이라 0으로 본다(별점은 1~5). DB마다 null 정렬 위치가 달라서 nulls last 대신 쓴다
            + " order by coalesce((select avg(r.score) from Rating r where r.contentId = c.id), 0) desc, c.id desc",
            countQuery = "select count(c) " + PUBLIC_FILTER)
    Page<RatingContent> findByVisibilityOrderByAverageScore(@Param("visibility") RatingContentVisibility visibility,
            @Param("category") RatingContentCategory category, @Param("subcategory") RatingContentSubcategory subcategory,
            @Param("status") RatingContentStatus status, Pageable pageable);
}
