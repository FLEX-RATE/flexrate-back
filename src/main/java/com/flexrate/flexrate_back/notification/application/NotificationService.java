package com.flexrate.flexrate_back.notification.application;

import com.flexrate.flexrate_back.notification.domain.Notification;
import com.flexrate.flexrate_back.notification.domain.repository.NotificationQueryRepository;
import com.flexrate.flexrate_back.notification.domain.repository.NotificationRepository;
import com.flexrate.flexrate_back.notification.dto.NotificationResponse;
import com.flexrate.flexrate_back.notification.dto.NotificationSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationQueryRepository notificationQueryRepository;

    private static final int PAGE_SIZE = 8;

    @Transactional(readOnly = true)
    public NotificationResponse getNotifications(Long memberId, Long lastNotificationId) {
        log.debug("알림 조회 시작: memberId={}, lastNotificationId={}", memberId, lastNotificationId);
        List<Notification> notifications = notificationQueryRepository.findNotifications(
                memberId,
                lastNotificationId,
                PAGE_SIZE + 1
        );

        boolean hasNext = notifications.size() > PAGE_SIZE;
        if (hasNext) {
            notifications = notifications.subList(0, PAGE_SIZE);
        }

        List<NotificationSummaryDto> notificationDtos = notifications.stream()
                .map(n -> new NotificationSummaryDto(
                        n.getNotificationId(),
                        n.getContent(),
                        n.getSentAt().toString(),
                        n.isRead(),
                        n.getType().name()))
                .toList();
        for (Notification n : notifications) {
            log.debug("알림 ID: {}, isRead: {}", n.getNotificationId(), n.isRead());
        }

        return new NotificationResponse(notificationDtos, hasNext);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다: " + notificationId));
        notification.markAsRead();
    }

    @Transactional
    public void deleteAll(Long memberId) {
        notificationRepository.deleteByMember_MemberId(memberId);
    }

    @Transactional
    public int countUnreadNotifications(Long memberId) {
        return notificationRepository.countByMember_MemberIdAndIsReadFalse(memberId);
    }
}