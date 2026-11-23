package com.trendizm.backend.item.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.item.application.dto.CreateItemCommand;
import com.trendizm.backend.item.application.dto.CreateItemQuestionCommand;
import com.trendizm.backend.item.application.dto.ItemResponse;
import com.trendizm.backend.item.application.port.ItemService;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;
import com.trendizm.backend.point.application.exception.PointErrorCode;
import com.trendizm.backend.point.application.port.PointService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

/**
 * 잠금 해제를 동시에 요청하거나 포인트가 부족할 때를 검사한다.
 * 요청마다 트랜잭션이 커밋되어야 해서 @Transactional을 붙이지 않고, 테스트가 끝나면 테이블을 비운다.
 */
@SpringBootTest
@Sql(statements = {"DELETE FROM point_histories", "DELETE FROM point_wallets", "DELETE FROM item_unlocks",
        "DELETE FROM items", "DELETE FROM item_questions"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ItemUnlockConcurrencyTest {

    private static final Long ASKER = 1L;
    private static final Long RECOMMENDER = 2L;
    private static final Long VIEWER = 3L;

    @Autowired
    private ItemService itemService;

    @Autowired
    private PointService pointService;

    private Long questionId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        questionId = itemService.createQuestion(
                new CreateItemQuestionCommand(ASKER, ItemSubcategory.CLOTHING, "양말 추천해주세요", "따뜻한 양말")).id();
        itemId = itemService.createRecommendation(new CreateItemCommand(questionId, RECOMMENDER, "포근 양말", null,
                null, null, "정말 따뜻해요", null, null)).id();
    }

    @Test
    @DisplayName("같은 회원이 같은 아이템을 동시에 여러 번 열어도 100P는 한 번만 빠지고 모두 열린 아이템을 받는다")
    void 동시_잠금_해제() throws Exception {
        pointService.rewardForReview(VIEWER, 1L, 300);

        List<Callable<ItemResponse>> requests = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            requests.add(() -> itemService.unlockRecommendation(itemId, VIEWER));
        }
        List<Object> results = runConcurrently(requests);

        assertThat(results).allSatisfy(result -> assertThat(result).isInstanceOfSatisfying(ItemResponse.class,
                response -> assertThat(response.locked()).isFalse()));
        assertThat(pointService.getBalance(VIEWER).balance()).isEqualTo(200);
        assertThat(pointService.getBalance(RECOMMENDER).balance()).isEqualTo(100);
    }

    @Test
    @DisplayName("포인트가 부족하면 INSUFFICIENT_POINT_BALANCE이고, 열람 기록도 남지 않아 계속 잠긴 상태다")
    void 포인트_부족() throws Exception {
        pointService.rewardForReview(VIEWER, 1L, 50);

        List<Object> results = runConcurrently(List.of(() -> itemService.unlockRecommendation(itemId, VIEWER)));

        assertThat(results).singleElement().isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(PointErrorCode.INSUFFICIENT_POINT_BALANCE));
        assertThat(itemService.getRecommendations(questionId, VIEWER).get(0).locked()).isTrue();
        assertThat(pointService.getBalance(VIEWER).balance()).isEqualTo(50);
        assertThat(pointService.getBalance(RECOMMENDER).balance()).isZero();

        // 충전한 뒤 다시 열면 정상적으로 열린다 (실패한 열람 기록이 막지 않는다)
        pointService.rewardForReview(VIEWER, 2L, 100);
        assertThat(itemService.unlockRecommendation(itemId, VIEWER).locked()).isFalse();
        assertThat(pointService.getBalance(VIEWER).balance()).isEqualTo(50);
    }

    /** 모든 요청을 동시에 시작한다. 결과는 성공하면 반환값, 실패하면 그 예외 */
    private static <T> List<Object> runConcurrently(List<Callable<T>> requests) throws Exception {
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
            List<Object> results = new ArrayList<>();
            for (Future<T> future : futures) {
                try {
                    results.add(future.get(30, TimeUnit.SECONDS));
                } catch (ExecutionException e) {
                    results.add(e.getCause());
                }
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }
}
