package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

@Builder
public record ConsentRequestDTO(
        String type,  // 동의 항목 타입 (예: "SERVICE", "MARKETING")
        boolean agreed // 동의 여부 (true / false)
) {}
