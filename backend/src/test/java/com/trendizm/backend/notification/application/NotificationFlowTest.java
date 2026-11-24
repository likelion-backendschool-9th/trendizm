package com.trendizm.backend.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.lookup.application.dto.AddMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.CreateMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.dto.MemeInterpretationResponse;
import com.trendizm.backend.lookup.application.dto.MemeVoteResult;
import com.trendizm.backend.lookup.application.dto.VoteMemeInterpretationCommand;
import com.trendizm.backend.lookup.application.port.MemeInterpretationService;
import com.trendizm.backend.lookup.domain.enums.MemeVoteType;
import com.trendizm.backend.member.application.dto.RegisterMemberCommand;
import com.trendizm.backend.member.application.port.MemberService;
import com.trendizm.backend.notification.adapter.out.sse.SseNotificationPusher;
import com.trendizm.backend.notification.application.dto.CreateNotificationCommand;
import com.trendizm.backend.notification.application.dto.NotificationResponse;
import com.trendizm.backend.notification.application.exception.NotificationErrorCode;
import com.trendizm.backend.notification.application.port.NotificationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;

/**
 * 투표·해석 작성이 커밋된 뒤 알림이 만들어지는지 검사한다.
 * 알림은 커밋 뒤(AFTER_COMMIT)에 만들어지므로 @Transactional을 붙이지 않고, 테스트가 끝나면 테이블을 비운다.
 */
@SpringBootTest
@Sql(statements = {"DELETE FROM notifications", "DELETE FROM meme_interpretation_votes",
        "DELETE FROM meme_interpretations", "DELETE FROM memes", "DELETE FROM users"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class NotificationFlowTest {

    @Autowired
    private MemeInterpretationService interpretationService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private NotificationService notificationService;

    @MockitoSpyBean
    private SseNotificationPusher sseEmitters;

    private Long writer;
    private Long voter;
    private Long otherVoter;
    private Long memeId;
    private Long interpretationId;

    /** writer가 "TMI" 문서를 만들며 해석 하나를 쓴다. 문서 주인도 writer다 */
    @BeforeEach
    void setUp() {
        writer = register("writer");
        voter = register("voter");
        otherVoter = register("other");
        MemeInterpretationResponse first = interpretationService.createInterpretation(new CreateMemeInterpretationCommand(
                "TMI", writer, "Too Much Information", "내용", "https://youtu.be/tmi01", null, null));
        memeId = first.memeId();
        interpretationId = first.id();
    }

    private Long register(String name) {
        return memberService.register(new RegisterMemberCommand(name + "@test.com", "encoded", name, false));
    }

    private MemeVoteResult vote(Long userId, MemeVoteType type) {
        return interpretationService.vote(new VoteMemeInterpretationCommand(interpretationId, userId, type));
    }

    private List<NotificationResponse> notificationsOf(Long receiverId) {
        return notificationService.getNotifications(receiverId, 0, 20).content();
    }

    @Test
    @DisplayName("내 문서에 내가 해석을 쓰면 알림이 없다")
    void 문서_주인이_쓴_해석() {
        assertThat(notificationsOf(writer)).isEmpty();
    }

    @Test
    @DisplayName("다른 사람이 동의하면 해석 작성자에게 알림이 생기고, 커밋 뒤 안 읽은 수를 실시간으로 보낸다")
    void 동의_알림() {
        vote(voter, MemeVoteType.AGREE);

        List<NotificationResponse> notifications = notificationsOf(writer);
        assertThat(notifications).hasSize(1);
        NotificationResponse notification = notifications.getFirst();
        assertThat(notification.type()).isEqualTo("meme-vote");
        assertThat(notification.memeId()).isEqualTo(memeId);
        assertThat(notification.interpretationId()).isEqualTo(interpretationId);
        assertThat(notification.count()).isEqualTo(1);
        assertThat(notification.message()).isEqualTo("‘tmi’에 쓴 해석에 동의 1개가 달렸어요.");
        assertThat(notification.read()).isFalse();
        assertThat(notificationService.getUnreadCount(writer)).isEqualTo(1);
        verify(sseEmitters).pushUnreadCount(writer, 1);
    }

    @Test
    @DisplayName("같은 해석의 안 읽은 동의 알림은 하나로 묶이고 동의 수만 바뀐다")
    void 동의_알림_묶기() {
        vote(voter, MemeVoteType.AGREE);
        vote(otherVoter, MemeVoteType.AGREE);

        List<NotificationResponse> notifications = notificationsOf(writer);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.getFirst().count()).isEqualTo(2);
        assertThat(notificationService.getUnreadCount(writer)).isEqualTo(1);
    }

    @Test
    @DisplayName("읽은 뒤에 동의가 또 오면 새 알림이 생긴다")
    void 읽은_뒤_새_동의() {
        vote(voter, MemeVoteType.AGREE);
        notificationService.markAsRead(writer, notificationsOf(writer).getFirst().id());

        vote(otherVoter, MemeVoteType.AGREE);

        List<NotificationResponse> notifications = notificationsOf(writer);
        assertThat(notifications).hasSize(2);
        assertThat(notifications.getFirst().read()).isFalse();
        assertThat(notifications.getFirst().count()).isEqualTo(2);
        assertThat(notificationService.getUnreadCount(writer)).isEqualTo(1);
    }

    @Test
    @DisplayName("비동의, 같은 쪽 재투표, 내 해석에 내 투표는 알림이 없다")
    void 알림_없는_투표() {
        vote(voter, MemeVoteType.DISAGREE);
        vote(writer, MemeVoteType.AGREE);
        vote(otherVoter, MemeVoteType.AGREE);
        vote(otherVoter, MemeVoteType.AGREE);

        List<NotificationResponse> notifications = notificationsOf(writer);
        assertThat(notifications).hasSize(1);
        // writer 본인의 동의도 동의 수에는 들어간다(알림만 안 만든다)
        assertThat(notifications.getFirst().count()).isEqualTo(2);
        // 알림 쪽에 넘어온 이벤트는 문서를 만들며 쓴 첫 해석, writer 본인 동의, otherVoter의 첫 동의 세 번뿐이다.
        // 비동의·같은 쪽 재투표는 이벤트도 없고, 본인 이벤트 두 개는 알림 쪽에서 거른다
        verify(notificationService, times(3)).notify(any(CreateNotificationCommand.class));
    }

    @Test
    @DisplayName("비동의에서 동의로 바꾸면 알림이 생긴다")
    void 비동의에서_동의로() {
        vote(voter, MemeVoteType.DISAGREE);
        vote(voter, MemeVoteType.AGREE);

        assertThat(notificationsOf(writer)).hasSize(1);
    }

    @Test
    @DisplayName("알림 수신을 끈 회원에게는 알림을 만들지 않는다")
    void 수신_끈_회원() {
        jdbcTemplate.update("UPDATE users SET notify = false WHERE id = ?", writer);

        vote(voter, MemeVoteType.AGREE);

        assertThat(notificationsOf(writer)).isEmpty();
        verify(sseEmitters, never()).pushUnreadCount(eq(writer), anyLong());
    }

    @Test
    @DisplayName("다른 사람이 내 문서에 해석을 쓰면 문서 주인에게 알림이 생긴다")
    void 새_해석_알림() {
        MemeInterpretationResponse added = interpretationService.addInterpretation(
                new AddMemeInterpretationCommand(memeId, voter, "다른 뜻", "내용"));

        List<NotificationResponse> notifications = notificationsOf(writer);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.getFirst().type()).isEqualTo("meme-interpretation");
        assertThat(notifications.getFirst().interpretationId()).isEqualTo(added.id());
        assertThat(notifications.getFirst().message()).isEqualTo("‘tmi’에 새 해석이 달렸어요.");
    }

    @Test
    @DisplayName("알림 저장이 실패해도 이미 커밋된 투표는 성공으로 응답한다")
    void 알림_실패해도_투표_성공() {
        doThrow(new IllegalStateException("알림 저장 실패")).when(notificationService).notify(any());

        MemeVoteResult result = vote(voter, MemeVoteType.AGREE);

        assertThat(result.agreeCount()).isEqualTo(1);
        assertThat(notificationsOf(writer)).isEmpty();
    }

    @Test
    @DisplayName("읽음 처리해도 목록 순서가 바뀌지 않는다")
    void 읽음_처리_순서() {
        interpretationService.addInterpretation(new AddMemeInterpretationCommand(memeId, voter, "뜻1", "내용"));
        interpretationService.addInterpretation(new AddMemeInterpretationCommand(memeId, otherVoter, "뜻2", "내용"));
        List<Long> before = notificationsOf(writer).stream().map(NotificationResponse::id).toList();

        notificationService.markAsRead(writer, before.getLast());

        List<NotificationResponse> after = notificationsOf(writer);
        assertThat(after).extracting(NotificationResponse::id).containsExactlyElementsOf(before);
        assertThat(after.getLast().read()).isTrue();
        assertThat(notificationService.getUnreadCount(writer)).isEqualTo(1);
    }

    @Test
    @DisplayName("남의 알림이나 없는 알림을 읽음 처리하면 404 NOTIFICATION_NOT_FOUND")
    void 남의_알림_읽음() {
        vote(voter, MemeVoteType.AGREE);
        Long notificationId = notificationsOf(writer).getFirst().id();

        assertThatThrownBy(() -> notificationService.markAsRead(voter, notificationId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        assertThatThrownBy(() -> notificationService.markAsRead(writer, notificationId + 999))
                .isInstanceOf(BusinessException.class);
        assertThat(notificationService.getUnreadCount(writer)).isEqualTo(1);
    }
}
