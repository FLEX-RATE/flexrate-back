package com.flexrate.flexrate_back.notification.event;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.notification.domain.Notification;
import com.flexrate.flexrate_back.notification.domain.repository.NotificationRepository;
import com.flexrate.flexrate_back.notification.application.NotificationEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final NotificationEmitterService notificationEmitterService;

    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("NotificationEvent received for memberId={}, type={}, content={}",
                event.getMember().getMemberId(), event.getType(), event.getContent());

        Notification notification = Notification.builder()
                .member(event.getMember())
                .type(event.getType())
                .content(event.getContent())
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();

        Notification saved;
        try {
            saved = notificationRepository.save(notification);
            log.info("Notification saved: id={}, memberId={}", saved.getNotificationId(), saved.getMember().getMemberId());
        } catch (Exception e) {
            log.error("Notification 저장 실패: memberId={}, error={}", event.getMember().getMemberId(), e.getMessage(), e);
            throw new FlexrateException(ErrorCode.NOTIFICATION_SAVE_FAILED);
        }

        notificationEmitterService.sendNotification(event.getMember().getMemberId(), saved);
        log.info("Notification sent via SSE: memberId={}, notificationId={}", saved.getMember().getMemberId(), saved.getNotificationId());
    }
}
