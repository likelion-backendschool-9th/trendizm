package com.trendizm.backend.point.application.dto;

import com.trendizm.backend.point.domain.PointWallet;

/** 보유 포인트. 예: { "balance": 1200 } */
public record PointBalanceResponse(int balance) {

    public static PointBalanceResponse from(PointWallet wallet) {
        return new PointBalanceResponse(wallet.getBalance());
    }

    /** 아직 지갑이 없는 회원 */
    public static PointBalanceResponse empty() {
        return new PointBalanceResponse(0);
    }
}
