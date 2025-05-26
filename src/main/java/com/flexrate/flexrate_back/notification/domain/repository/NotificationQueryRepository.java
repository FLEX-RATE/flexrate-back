package com.flexrate.flexrate_back.notification.domain.repository;

import com.flexrate.flexrate_back.notification.domain.Notification;

import java.util.List;

public interface NotificationQueryRepository {
    List<Notification> findNotifications(Long memberId, Long lastNotificationId, int limit);
}
