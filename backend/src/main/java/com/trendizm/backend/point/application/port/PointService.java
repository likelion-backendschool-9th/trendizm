package com.trendizm.backend.point.application.port;

import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.point.application.dto.PointBalanceResponse;
import com.trendizm.backend.point.application.dto.PointHistoryResponse;
import com.trendizm.backend.point.application.dto.UnlockPaymentCommand;

/**
 * 포인트 유스케이스 (in 포트). 다른 도메인은 포인트를 이 인터페이스로만 바꾼다.
 * point는 다른 도메인을 호출하지 않는다 (순환 방지).
 * PG 충전·환불(7.2, 7.4)은 다음 작업에서 메서드를 추가한다.
 */
public interface PointService {

    /** 지갑이 없으면 0 */
    PointBalanceResponse getBalance(Long userId);

    /** 최신순. page는 0부터 */
    PageResponse<PointHistoryResponse> getHistories(Long userId, PointHistoryFilter filter, int page, int size);

    /**
     * 추천 아이템 열람 결제. 열람자 차감(UNLOCK)과 등록자 적립(EARN)을 호출한 쪽의 트랜잭션 안에서 함께 처리한다.
     * 두 지갑을 회원 id 순서로 잠가서, 동시에 서로의 아이템을 열람해도 교착 상태가 생기지 않는다.
     * 잔액이 부족하면 INSUFFICIENT_POINT_BALANCE, 같은 itemUnlockId로 이미 결제했으면 POINT_ALREADY_PAID.
     */
    void payForUnlock(UnlockPaymentCommand command);

    /**
     * 후기 작성 보상(REWARD). 후기 하나에 한 번만 지급하고, 이미 지급했으면 아무것도 하지 않는다.
     * 호불호 평점(rating)이 후기를 처음 작성할 때 호출한다.
     */
    void rewardForReview(Long userId, Long ratingId, int amount);
}
