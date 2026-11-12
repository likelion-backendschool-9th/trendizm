package com.trendizm.backend.notification.application.port.in;

import com.trendizm.backend.notification.application.dto.NotificationResponse;
import com.trendizm.backend.notification.application.port.NotificationService;
import com.trendizm.backend.notification.application.port.out.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Page<NotificationResponse> getNotifications(Pageable pageable) {
        // TODO(notification): 알림 목록 조회
        //  1. notificationRepository.findAll(pageable)
        //  2. .map(NotificationResponse::from) 으로 변환해 반환
        //  (숏폼·롱폼 탭, 해시태그 조회는 컬럼이 정해지면 NotificationRepository에 findByXxx를 추가)
        throw new UnsupportedOperationException("TODO(notification): 알림 목록 조회");
    }

    @Override
    public NotificationResponse getNotification(Long notificationId) {
        // TODO(notification): 알림 단건 조회
        //  1. notificationRepository.findById(notificationId), 없으면 예외를 던진다
        //  2. NotificationResponse.from(notification) 반환
        throw new UnsupportedOperationException("TODO(notification): 알림 단건 조회");
    }

    @Override
    @Transactional
    public Long createNotification() {
        // TODO(notification): 알림 등록
        //  1. Notification.create(...) (영상 링크 → 임베드 URL 변환은 여기서)
        //  2. notificationRepository.save(notification).getId() 반환
        throw new UnsupportedOperationException("TODO(notification): 알림 등록");
    }

    @Override
    @Transactional
    public void updateNotification(Long notificationId) {
        // TODO(notification): 알림 수정
        //  1. notificationRepository.findById(notificationId), 없으면 NOT_FOUND
        //  2. Notification에 update(...) 메서드를 만들어 호출 (트랜잭션 안이라 변경 감지로 저장된다)
        throw new UnsupportedOperationException("TODO(notification): 알림 수정");
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        // TODO(notification): 알림 삭제
        //  1. notificationRepository.findById(notificationId), 없으면 NOT_FOUND
        //  2. notificationRepository.delete(notification)
        throw new UnsupportedOperationException("TODO(notification): 알림 삭제");
    }
}
