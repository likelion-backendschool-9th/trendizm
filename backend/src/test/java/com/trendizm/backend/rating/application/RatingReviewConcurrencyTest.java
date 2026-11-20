package com.trendizm.backend.rating.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.point.application.port.PointService;
import com.trendizm.backend.rating.application.dto.RatingReviewQuery;
import com.trendizm.backend.rating.application.dto.RatingReviewSaveResult;
import com.trendizm.backend.rating.application.dto.RegisterRatingContentCommand;
import com.trendizm.backend.rating.application.dto.SaveRatingReviewCommand;
import com.trendizm.backend.rating.application.port.RatingService;
import com.trendizm.backend.rating.domain.enums.Sentiment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

/**
 * 같은 회원이 같은 콘텐츠에 후기를 동시에 처음 저장할 때를 검사한다 (예: 작성 버튼을 두 번 누름).
 * 스레드마다 트랜잭션이 커밋되어야 해서 @Transactional을 붙이지 않고, 테스트가 끝나면 테이블을 비운다.
 */
@SpringBootTest
@Sql(statements = {"DELETE FROM point_histories", "DELETE FROM point_wallets", "DELETE FROM ratings",
        "DELETE FROM rating_contents"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RatingReviewConcurrencyTest {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("같은 후기를 동시에 처음 저장해도 후기는 하나만 생기고 보상도 한 번만 준다")
    void 동시_첫_작성() throws Exception {
        Long contentId = ratingService.registerContent(new RegisterRatingContentCommand(
                "drama", "ongoing", "드라마", null, null, null, null, null)).id();
        Long userId = 7L;

        List<Callable<RatingReviewSaveResult>> requests = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            requests.add(() -> ratingService.saveReview(new SaveRatingReviewCommand(contentId, userId,
                    Sentiment.LIKE, 5, "제목", "본문", null)));
        }
        List<RatingReviewSaveResult> results = runConcurrently(requests);

        assertThat(results).hasSize(4);
        assertThat(results).filteredOn(RatingReviewSaveResult::created).hasSize(1);
        assertThat(ratingService.getReviews(new RatingReviewQuery(contentId, null, null, null, 0, 20))
                .totalElements()).isEqualTo(1);
        assertThat(pointService.getBalance(userId).balance()).isEqualTo(10);
    }

    /** 모든 요청을 동시에 시작하고 결과를 모은다. 하나라도 실패하면 그 예외를 던진다 */
    private static <T> List<T> runConcurrently(List<Callable<T>> requests) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(requests.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> request : requests) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return request.call();
                }));
            }
            start.countDown();
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(30, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }
}
