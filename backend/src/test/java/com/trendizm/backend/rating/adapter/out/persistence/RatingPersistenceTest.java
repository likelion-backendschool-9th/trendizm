package com.trendizm.backend.rating.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.rating.application.port.out.RatingContentRepository;
import com.trendizm.backend.rating.application.port.out.RatingLikeRepository;
import com.trendizm.backend.rating.application.port.out.RatingRepository;
import com.trendizm.backend.rating.domain.entity.Rating;
import com.trendizm.backend.rating.domain.entity.RatingContent;
import com.trendizm.backend.rating.domain.entity.RatingLike;
import com.trendizm.backend.rating.domain.enums.RatingContentStatus;
import com.trendizm.backend.rating.domain.enums.RatingContentSubcategory;
import com.trendizm.backend.rating.domain.enums.Sentiment;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

/** 칼럼 매핑(jsonb, 날짜)과 유니크 제약을 실제 DB로 확인한다 */
@SpringBootTest
@Transactional
class RatingPersistenceTest {

    @Autowired
    private RatingContentRepository contentRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private RatingLikeRepository likeRepository;

    @Autowired
    private EntityManager entityManager;

    private Long contentId;

    @BeforeEach
    void setUp() {
        contentId = contentRepository.save(RatingContent.create(RatingContentSubcategory.DRAMA,
                RatingContentStatus.ONGOING, "드라마", "설명", "https://img.test/a.jpg", 15,
                LocalDate.of(2026, 11, 1), Map.of("season", 2, "episodeLength", "70분"))).getId();
    }

    @Test
    @DisplayName("콘텐츠의 추가 정보(jsonb)와 공개일, 생성 시각이 저장된다")
    void 콘텐츠_저장() {
        entityManager.flush();
        entityManager.clear();

        RatingContent found = contentRepository.findById(contentId).orElseThrow();

        assertThat(found.getExtra()).containsEntry("season", 2).containsEntry("episodeLength", "70분");
        assertThat(found.getReleasedAt()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("같은 회원이 같은 콘텐츠에 후기를 두 개 쓸 수 없다")
    void 후기_중복() {
        ratingRepository.save(Rating.create(contentId, 1L, Sentiment.LIKE, 5, "최고", "재밌어요", null));
        entityManager.flush();

        assertThatThrownBy(() -> {
            ratingRepository.save(Rating.create(contentId, 1L, Sentiment.DISLIKE, 1, "별로", "아쉬워요", null));
            entityManager.flush();
        }).isInstanceOfAny(DataIntegrityViolationException.class, org.hibernate.exception.ConstraintViolationException.class);
    }

    @Test
    @DisplayName("같은 회원이 같은 후기에 두 번 공감할 수 없다")
    void 공감_중복() {
        Long ratingId = ratingRepository.save(
                Rating.create(contentId, 1L, Sentiment.LIKE, 5, "최고", "재밌어요", null)).getId();
        likeRepository.save(RatingLike.create(ratingId, 2L));
        entityManager.flush();

        assertThatThrownBy(() -> {
            likeRepository.save(RatingLike.create(ratingId, 2L));
            entityManager.flush();
        }).isInstanceOfAny(DataIntegrityViolationException.class, org.hibernate.exception.ConstraintViolationException.class);
    }
}
