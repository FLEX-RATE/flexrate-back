package com.flexrate.flexrate_back.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationSummaryDto {
    private Long id;
    private String content;
    private String sentAt;

    @JsonProperty("isRead")
    private boolean isRead;

    private String type;
}
