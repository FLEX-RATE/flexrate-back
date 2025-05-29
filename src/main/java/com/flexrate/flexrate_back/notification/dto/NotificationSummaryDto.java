package com.flexrate.flexrate_back.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flexrate.flexrate_back.notification.domain.Notification;

public record NotificationSummaryDto(
        Long id,
        String content,
        String sentAt,
        @JsonProperty("isRead") boolean isRead,
        String type
) {
    public static NotificationSummaryDto from(Notification notification) {
        return new NotificationSummaryDto(
                notification.getNotificationId(),
                notification.getContent(),
                notification.getSentAt().toString(),
                notification.isRead(),
                notification.getType().name()
        );
    }
}
