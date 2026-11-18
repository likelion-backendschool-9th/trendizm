package com.trendizm.backend.point.application.dto;

/**
 * 추천 아이템 열람 결제에 필요한 값. 트렌드 아이템(item)이 PointService.payForUnlock으로 넘긴다.
 *
 * @param payerId      열람한 회원 (price만큼 차감)
 * @param earnerId     추천 아이템 등록자 (earnAmount만큼 적립)
 * @param itemUnlockId item_unlocks.id. 같은 id로는 한 번만 결제된다
 * @param price        열람 가격 (0보다 커야 한다)
 * @param earnAmount   등록자 적립액 (0이면 적립하지 않는다). 적립 비율은 item에서 정한다
 */
public record UnlockPaymentCommand(Long payerId, Long earnerId, Long itemUnlockId, int price, int earnAmount) {
}
