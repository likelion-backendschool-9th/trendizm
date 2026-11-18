package com.trendizm.backend.point.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.point.application.dto.PointBalanceResponse;
import com.trendizm.backend.point.application.dto.PointHistoryResponse;
import com.trendizm.backend.point.application.dto.UnlockPaymentCommand;
import com.trendizm.backend.point.application.exception.PointErrorCode;
import com.trendizm.backend.point.application.port.PointHistoryFilter;
import com.trendizm.backend.point.application.port.PointService;
import com.trendizm.backend.point.application.port.out.PointHistoryRepository;
import com.trendizm.backend.point.application.port.out.PointWalletRepository;
import com.trendizm.backend.point.domain.entity.PointHistory;
import com.trendizm.backend.point.domain.entity.PointWallet;
import com.trendizm.backend.point.domain.enums.PointType;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 잔액을 바꾸는 메서드는 지갑 행을 잠근 뒤(findByUserIdForUpdate) 바꾸고, 같은 트랜잭션에서 내역을 남긴다.
 * 다른 도메인이 호출하면 호출한 쪽의 트랜잭션에 함께 묶인다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointServiceImpl implements PointService {

    private static final Sort LATEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final PointWalletRepository walletRepository;
    private final PointHistoryRepository historyRepository;

    @Override
    public PointBalanceResponse getBalance(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(PointBalanceResponse::from)
                .orElseGet(PointBalanceResponse::empty);
    }

    @Override
    public PageResponse<PointHistoryResponse> getHistories(Long userId, PointHistoryFilter filter, int page, int size) {
        return PageResponse.from(
                historyRepository.findByUserIdAndTypeIn(userId, typesOf(filter), PageRequest.of(page, size, LATEST_FIRST)),
                PointHistoryResponse::from);
    }

    @Override
    @Transactional
    public void payForUnlock(UnlockPaymentCommand command) {
        validate(command);
        Long payerId = command.payerId();
        Long earnerId = command.earnerId();
        boolean earns = command.earnAmount() > 0;

        // 교착 상태를 막으려고 지갑을 항상 회원 id가 작은 쪽부터 잠근다
        Set<Long> userIds = new TreeSet<>();
        userIds.add(payerId);
        if (earns) {
            userIds.add(earnerId);
        }
        Map<Long, PointWallet> wallets = new HashMap<>();
        for (Long userId : userIds) {
            PointWallet wallet = earns && userId.equals(earnerId)
                    ? lockOrCreateWallet(userId)
                    : walletRepository.findByUserIdForUpdate(userId).orElse(null);
            wallets.put(userId, wallet);
        }

        // 잠근 뒤에 확인해야 같은 열람을 동시에 결제하는 요청도 한 번만 통과한다
        if (historyRepository.existsByItemUnlockIdAndType(command.itemUnlockId(), PointType.UNLOCK)) {
            throw new BusinessException(PointErrorCode.POINT_ALREADY_PAID);
        }

        PointWallet payer = wallets.get(payerId);
        if (payer == null || !payer.canDeduct(command.price())) {
            throw new BusinessException(PointErrorCode.INSUFFICIENT_POINT_BALANCE);
        }
        payer.deduct(command.price());
        historyRepository.save(PointHistory.create(payer, PointType.UNLOCK, command.price(), command.itemUnlockId()));

        if (earns) {
            PointWallet earner = wallets.get(earnerId);
            earner.earn(command.earnAmount());
            historyRepository.save(PointHistory.create(earner, PointType.EARN, command.earnAmount(), command.itemUnlockId()));
        }
    }

    @Override
    @Transactional
    public void rewardForReview(Long userId, Long ratingId, int amount) {
        if (userId == null || ratingId == null || amount <= 0) {
            throw new IllegalArgumentException(
                    "보상 값이 올바르지 않습니다. userId=" + userId + ", ratingId=" + ratingId + ", amount=" + amount);
        }
        PointWallet wallet = lockOrCreateWallet(userId);
        // 지갑을 잠근 뒤에 확인해서, 같은 후기 보상이 동시에 들어와도 한 번만 지급한다
        if (historyRepository.existsByRatingIdAndType(ratingId, PointType.REWARD)) {
            return;
        }
        wallet.earn(amount);
        historyRepository.save(PointHistory.create(wallet, PointType.REWARD, amount, ratingId));
    }

    /**
     * 지갑이 없으면 먼저 만들고(INSERT ... ON CONFLICT DO NOTHING), 잠가서 가져온다.
     * 같은 회원의 첫 지갑을 동시에 만들어도, 늦은 쪽은 아무것도 하지 않고 만들어진 지갑을 잠근다.
     */
    private PointWallet lockOrCreateWallet(Long userId) {
        walletRepository.insertIfAbsent(userId);
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalStateException("지갑을 만들지 못했습니다. userId=" + userId));
    }

    private static void validate(UnlockPaymentCommand command) {
        if (command.payerId() == null || command.earnerId() == null || command.itemUnlockId() == null
                || command.price() <= 0 || command.earnAmount() < 0) {
            throw new IllegalArgumentException("열람 결제 값이 올바르지 않습니다. " + command);
        }
    }

    private static Set<PointType> typesOf(PointHistoryFilter filter) {
        return switch (filter == null ? PointHistoryFilter.ALL : filter) {
            case ALL -> EnumSet.allOf(PointType.class);
            case EARN -> EnumSet.of(PointType.EARN, PointType.REWARD);
            case USE -> EnumSet.of(PointType.UNLOCK);
            case CHARGE -> EnumSet.of(PointType.CHARGE, PointType.REFUND);
        };
    }
}
