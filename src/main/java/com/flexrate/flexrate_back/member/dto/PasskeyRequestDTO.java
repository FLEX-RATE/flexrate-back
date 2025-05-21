package com.flexrate.flexrate_back.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PasskeyRequestDTO(
        Long credentialId,   // Base64url 인코딩된 credential ID
        String publicKey,      // PEM 형식의 공개키 (추출 필요)
        long signCount,        // 장치의 서명 카운터
        String deviceInfo      // 사용자의 브라우저 또는 장치 정보 (예: Chrome on Windows)
) {
        @Builder
        public PasskeyRequestDTO {}
}
