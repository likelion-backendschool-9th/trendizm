package com.trendizm.backend.point.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.point.application.dto.PointHistoryResponse;
import com.trendizm.backend.point.application.dto.UnlockPaymentCommand;
import com.trendizm.backend.point.application.exception.PointErrorCode;
import com.trendizm.backend.point.application.port.PointHistoryFilter;
import com.trendizm.backend.point.application.port.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** 지갑 잔액은 후기 보상(rewardForReview)으로 채운다. 충전(7.2)은 다음 작업에서 추가한다 */
@SpringBootTest
@Transactional
class PointServiceTest {

    private static final Long PAYER = 1L;
    private static final Long EARNER = 2L;

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("지갑이 없는 회원의 잔액은 0이다")
    void 지갑_없음() {
        assertThat(pointService.getBalance(99L).balance()).isZero();
    }

    @Test
    @DisplayName("후기 보상을 받으면 지갑이 생기고 잔액과 내역이 남는다")
    void 후기_보상() {
        pointService.rewardForReview(PAYER, 10L, 50);

        assertThat(pointService.getBalance(PAYER).balance()).isEqualTo(50);
        PageResponse<PointHistoryResponse> histories = pointService.getHistories(PAYER, PointHistoryFilter.ALL, 0, 20);
        assertThat(histories.content()).singleElement().satisfies(history -> {
            assertThat(history.type()).isEqualTo("REWARD");
            assertThat(history.amount()).isEqualTo(50);
            assertThat(history.balanceAfter()).isEqualTo(50);
        });
    }

    @Test
    @DisplayName("같은 후기 보상은 한 번만 지급한다")
    void 후기_보상_중복() {
        pointService.rewardForReview(PAYER, 10L, 50);
        pointService.rewardForReview(PAYER, 10L, 50);

        assertThat(pointService.getBalance(PAYER).balance()).isEqualTo(50);
        assertThat(pointService.getHistories(PAYER, PointHistoryFilter.ALL, 0, 20).totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("열람 결제는 열람자에서 차감하고 등록자에게 적립한다")
    void 열람_결제() {
        pointService.rewardForReview(PAYER, 10L, 300);

        pointService.payForUnlock(new UnlockPaymentCommand(PAYER, EARNER, 100L, 100, 100));

        assertThat(pointService.getBalance(PAYER).balance()).isEqualTo(200);
        assertThat(pointService.getBalance(EARNER).balance()).isEqualTo(100);
        assertThat(pointService.getHistories(PAYER, PointHistoryFilter.USE, 0, 20).content())
                .singleElement()
                .satisfies(history -> {
                    assertThat(history.type()).isEqualTo("UNLOCK");
                    assertThat(history.amount()).isEqualTo(-100);
                    assertThat(history.balanceAfter()).isEqualTo(200);
                });
        assertThat(pointService.getHistories(EARNER, PointHistoryFilter.EARN, 0, 20).content())
                .singleElement()
                .satisfies(history -> assertThat(history.type()).isEqualTo("EARN"));
    }

    @Test
    @DisplayName("적립액이 0이면 등록자에게 적립하지 않는다")
    void 적립_없음() {
        pointService.rewardForReview(PAYER, 10L, 300);

        pointService.payForUnlock(new UnlockPaymentCommand(PAYER, EARNER, 100L, 100, 0));

        assertThat(pointService.getBalance(PAYER).balance()).isEqualTo(200);
        assertThat(pointService.getBalance(EARNER).balance()).isZero();
        assertThat(pointService.getHistories(EARNER, PointHistoryFilter.ALL, 0, 20).totalElements()).isZero();
    }

    @Test
    @DisplayName("잔액이 부족하면 INSUFFICIENT_POINT_BALANCE이고 아무것도 바뀌지 않는다")
    void 잔액_부족() {
        pointService.rewardForReview(PAYER, 10L, 50);

        assertThatThrownBy(() -> pointService.payForUnlock(new UnlockPaymentCommand(PAYER, EARNER, 100L, 100, 100)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(PointErrorCode.INSUFFICIENT_POINT_BALANCE);
        assertThat(pointService.getBalance(PAYER).balance()).isEqualTo(50);
        assertThat(pointService.getBalance(EARNER).balance()).isZero();
    }

    @Test
    @DisplayName("지갑이 없는 회원이 열람하면 INSUFFICIENT_POINT_BALANCE")
    void 지갑_없이_열람() {
        assertThatThrownBy(() -> pointService.payForUnlock(new UnlockPaymentCommand(PAYER, EARNER, 100L, 100, 100)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(PointErrorCode.INSUFFICIENT_POINT_BALANCE);
    }

    @Test
    @DisplayName("같은 열람 id로 두 번 결제하면 POINT_ALREADY_PAID")
    void 중복_결제() {
        pointService.rewardForReview(PAYER, 10L, 300);
        pointService.payForUnlock(new UnlockPaymentCommand(PAYER, EARNER, 100L, 100, 100));

        assertThatThrownBy(() -> pointService.payForUnlock(new UnlockPaymentCommand(PAYER, EARNER, 100L, 100, 100)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(PointErrorCode.POINT_ALREADY_PAID);
        assertThat(pointService.getBalance(PAYER).balance()).isEqualTo(200);
    }

    @Test
    @DisplayName("내역은 최신순이고, 탭별로 걸러진다")
    void 내역_필터와_정렬() {
        pointService.rewardForReview(PAYER, 10L, 300);
        pointService.payForUnlock(new UnlockPaymentCommand(PAYER, EARNER, 100L, 100, 100));
        pointService.rewardForReview(PAYER, 11L, 20);

        assertThat(pointService.getHistories(PAYER, PointHistoryFilter.ALL, 0, 20).content())
                .extracting(PointHistoryResponse::type)
                .containsExactly("REWARD", "UNLOCK", "REWARD");
        assertThat(pointService.getHistories(PAYER, PointHistoryFilter.EARN, 0, 20).totalElements()).isEqualTo(2);
        assertThat(pointService.getHistories(PAYER, PointHistoryFilter.USE, 0, 20).totalElements()).isEqualTo(1);
        assertThat(pointService.getHistories(PAYER, PointHistoryFilter.CHARGE, 0, 20).totalElements()).isZero();
    }

    @Test
    @DisplayName("잘못된 결제 값은 IllegalArgumentException")
    void 잘못된_결제_값() {
        assertThatThrownBy(() -> pointService.payForUnlock(new UnlockPaymentCommand(PAYER, EARNER, 100L, 0, 0)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> pointService.payForUnlock(new UnlockPaymentCommand(PAYER, EARNER, null, 100, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
