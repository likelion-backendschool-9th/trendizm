package com.trendizm.backend.accuse.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.accuse.application.dto.CreateAccuseCommand;
import com.trendizm.backend.accuse.application.exception.AccuseErrorCode;
import com.trendizm.backend.accuse.application.port.AccuseService;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.error.ErrorCode;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

/** 트랜잭션이 각각 커밋되어야 해서 @Transactional을 붙이지 않고, 끝나면 직접 지운다 */
@SpringBootTest
@Sql(statements = "DELETE FROM accuses", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AccuseConcurrencyTest {

    private static final int REQUESTS = 8;

    @Autowired
    private AccuseService accuseService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("같은 사람이 같은 대상을 동시에 여러 번 신고해도 한 건만 저장되고, 나머지는 모두 ACCUSE_ALREADY_EXISTS")
    void 동시_중복_신고() throws Exception {
        CreateAccuseCommand command = new CreateAccuseCommand("memes", 10L, 99L, 1L, "hate", null);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(REQUESTS);
        try {
            List<Future<ErrorCode>> results = new ArrayList<>();
            for (int i = 0; i < REQUESTS; i++) {
                Callable<ErrorCode> request = () -> {
                    start.await();
                    try {
                        accuseService.accuse(command);
                        return null;
                    } catch (BusinessException e) {
                        return e.getErrorCode();
                    }
                };
                results.add(executor.submit(request));
            }
            start.countDown();

            List<ErrorCode> errors = new ArrayList<>();
            for (Future<ErrorCode> result : results) {
                errors.add(result.get(10, TimeUnit.SECONDS));
            }
            assertThat(errors).filteredOn(error -> error == null).hasSize(1);
            assertThat(errors).filteredOn(error -> error != null)
                    .hasSize(REQUESTS - 1)
                    .containsOnly(AccuseErrorCode.ACCUSE_ALREADY_EXISTS);
        } finally {
            executor.shutdownNow();
        }
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accuses", Long.class)).isEqualTo(1);
    }
}
