package com.flexrate.flexrate_back.notification.dto;

import java.util.List;

public record NotificationResponse(
        List<NotificationSummaryDto> notifications,
        boolean hasNext
) {}