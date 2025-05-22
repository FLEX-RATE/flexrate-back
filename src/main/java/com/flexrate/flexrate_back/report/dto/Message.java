package com.flexrate.flexrate_back.report.dto;

import lombok.Builder;

@Builder
public record Message(
        String role,
        String content
) {
}
