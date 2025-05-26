package com.flexrate.flexrate_back.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationResponseDto {
    private List<NotificationSummaryDto> notifications;
    private boolean hasNext;
}
