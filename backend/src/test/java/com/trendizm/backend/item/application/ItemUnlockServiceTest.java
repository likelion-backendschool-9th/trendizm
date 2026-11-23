package com.trendizm.backend.item.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.item.application.dto.CreateItemCommand;
import com.trendizm.backend.item.application.dto.CreateItemQuestionCommand;
import com.trendizm.backend.item.application.dto.ItemResponse;
import com.trendizm.backend.item.application.exception.ItemErrorCode;
import com.trendizm.backend.item.application.port.ItemService;
import com.trendizm.backend.item.domain.enums.ItemSubcategory;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import com.trendizm.backend.member.application.port.MemberService;
import com.trendizm.backend.point.application.dto.PointHistoryResponse;
import com.trendizm.backend.point.application.port.PointHistoryFilter;
import com.trendizm.backend.point.application.port.PointService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 잠금 해제 성공 흐름. 포인트가 부족할 때 열람 기록까지 롤백되는지는 실제 커밋이 필요해서
 * ItemUnlockConcurrencyTest에서 확인한다.
 */
@SpringBootTest
@Transactional
class ItemUnlockServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PointService pointService;

    private Long asker;
    private Long recommender;
    private Long viewer;
    private Long questionId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        asker = memberService.register(new RegisterMemberCommand("asker@test.com", "encoded", "질문자", false));
        recommender = memberService.register(new RegisterMemberCommand("rec@test.com", "encoded", "추천자", false));
        viewer = memberService.register(new RegisterMemberCommand("viewer@test.com", "encoded", "열람자", false));
        questionId = itemService.createQuestion(
                new CreateItemQuestionCommand(asker, ItemSubcategory.CLOTHING, "양말 추천해주세요", "따뜻한 양말")).id();
        itemId = itemService.createRecommendation(new CreateItemCommand(questionId, recommender, "포근 양말", "무신사",
                12000, List.of("화이트"), "정말 따뜻해요", "https://img.test/a.jpg", "https://shop.test/a")).id();
        // 열람자 지갑에 300P를 넣어 둔다 (충전 기능 전이라 후기 보상으로 채운다)
        pointService.rewardForReview(viewer, 1L, 300);
    }

    private ItemResponse recommendationSeenBy(Long viewerId) {
        return itemService.getRecommendations(questionId, viewerId).get(0);
    }

    @Test
    @DisplayName("잠금 해제하면 열람자에게서 100P를 빼고 등록자에게 100P를 넣고, 열린 아이템을 돌려준다")
    void 잠금_해제() {
        ItemResponse opened = itemService.unlockRecommendation(itemId, viewer);

        assertThat(opened.locked()).isFalse();
        assertThat(opened.title()).isEqualTo("포근 양말");
        assertThat(opened.linkUrl()).isEqualTo("https://shop.test/a");
        assertThat(opened.mine()).isFalse();
        assertThat(pointService.getBalance(viewer).balance()).isEqualTo(200);
        assertThat(pointService.getBalance(recommender).balance()).isEqualTo(100);
        assertThat(pointService.getHistories(viewer, PointHistoryFilter.USE, 0, 20).content())
                .singleElement().extracting(PointHistoryResponse::type).isEqualTo("UNLOCK");
        assertThat(pointService.getHistories(recommender, PointHistoryFilter.EARN, 0, 20).content())
                .singleElement().extracting(PointHistoryResponse::type).isEqualTo("EARN");
    }

    @Test
    @DisplayName("열람한 회원에게는 목록에서도 열린 상태로 보이고, 다른 회원과 비로그인에게는 잠긴 상태다")
    void 열람_후_목록() {
        itemService.unlockRecommendation(itemId, viewer);

        assertThat(recommendationSeenBy(viewer).locked()).isFalse();
        assertThat(recommendationSeenBy(viewer).title()).isEqualTo("포근 양말");
        assertThat(recommendationSeenBy(asker).locked()).isTrue();
        assertThat(recommendationSeenBy(null).locked()).isTrue();
    }

    @Test
    @DisplayName("이미 열람한 아이템을 다시 열면 포인트를 쓰지 않는다")
    void 다시_열람() {
        itemService.unlockRecommendation(itemId, viewer);

        ItemResponse again = itemService.unlockRecommendation(itemId, viewer);

        assertThat(again.locked()).isFalse();
        assertThat(pointService.getBalance(viewer).balance()).isEqualTo(200);
        assertThat(pointService.getBalance(recommender).balance()).isEqualTo(100);
    }

    @Test
    @DisplayName("내가 등록한 아이템은 포인트 없이 열린 상태로 돌려준다")
    void 내_아이템() {
        ItemResponse own = itemService.unlockRecommendation(itemId, recommender);

        assertThat(own.locked()).isFalse();
        assertThat(own.mine()).isTrue();
        assertThat(pointService.getBalance(recommender).balance()).isZero();
    }

    @Test
    @DisplayName("없는 아이템이나 숨긴 질문 글의 아이템이면 ITEM_RECOMMENDATION_NOT_FOUND이고 포인트는 그대로다")
    void 대상_없음() {
        itemService.changeQuestionVisibility(questionId, false);

        for (Long target : List.of(999_999L, itemId)) {
            assertThatThrownBy(() -> itemService.unlockRecommendation(target, viewer))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ItemErrorCode.ITEM_RECOMMENDATION_NOT_FOUND);
        }
        assertThat(pointService.getBalance(viewer).balance()).isEqualTo(300);
    }
}
