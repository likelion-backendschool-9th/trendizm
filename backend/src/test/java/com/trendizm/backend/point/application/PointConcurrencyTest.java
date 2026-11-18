package com.trendizm.backend.point.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.point.application.dto.UnlockPaymentCommand;
import com.trendizm.backend.point.application.exception.PointErrorCode;
import com.trendizm.backend.point.application.port.PointService;
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
 * 여러 요청이 동시에 같은 지갑을 바꿀 때를 검사한다.
 * 스레드마다 트랜잭션이 커밋되어야 해서 @Transactional을 붙이지 않고, 테스트가 끝나면 테이블을 비운다.
 */
@SpringBootTest
@Sql(statements = {"DELETE FROM point_histories", "DELETE FROM point_wallets"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PointConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("잔액 300으로 100P 열람을 동시에 6번 요청하면 3번만 성공하고 잔액은 0이 된다")
    void 동시_차감() throws Exception {
        Long payer = 1L;
        Long earner = 2L;
        pointService.rewardForReview(payer, 1L, 300);

        List<Callable<Boolean>> requests = new ArrayList<>();
        for (long i = 0; i < 6; i++) {
            long itemUnlockId = 100 + i;
            requests.add(() -> {
                pointService.payForUnlock(new UnlockPaymentCommand(payer, earner, itemUnlockId, 100, 100));
                return true;
            });
        }
        List<Throwable> failures = runConcurrently(requests);

        assertThat(failures).hasSize(3).allSatisfy(failure -> assertThat(failure)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(PointErrorCode.INSUFFICIENT_POINT_BALANCE));
        assertThat(pointService.getBalance(payer).balance()).isZero();
        assertThat(pointService.getBalance(earner).balance()).isEqualTo(300);
    }

    @Test
    @DisplayName("두 회원이 동시에 서로의 아이템을 열람해도 교착 상태 없이 둘 다 성공한다")
    void 서로_열람() throws Exception {
        Long userA = 1L;
        Long userB = 2L;
        pointService.rewardForReview(userA, 1L, 100);
        pointService.rewardForReview(userB, 2L, 100);

        List<Throwable> failures = runConcurrently(List.of(
                () -> {
                    pointService.payForUnlock(new UnlockPaymentCommand(userA, userB, 100L, 100, 100));
                    return true;
                },
                () -> {
                    pointService.payForUnlock(new UnlockPaymentCommand(userB, userA, 101L, 100, 100));
                    return true;
                }));

        assertThat(failures).isEmpty();
        assertThat(pointService.getBalance(userA).balance()).isEqualTo(100);
        assertThat(pointService.getBalance(userB).balance()).isEqualTo(100);
    }

    /** 모든 요청을 한 번에 출발시키고, 실패한 요청의 예외 목록을 돌려준다 */
    private static List<Throwable> runConcurrently(List<Callable<Boolean>> requests) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(requests.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (Callable<Boolean> request : requests) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return request.call();
                }));
            }
            start.countDown();
            List<Throwable> failures = new ArrayList<>();
            for (Future<Boolean> future : futures) {
                try {
                    future.get(30, TimeUnit.SECONDS);
                } catch (java.util.concurrent.ExecutionException e) {
                    failures.add(e.getCause());
                }
            }
            return failures;
        } finally {
            executor.shutdownNow();
        }
    }
}
