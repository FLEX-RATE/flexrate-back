package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

@Builder
public record PasskeyRequestDTO(
        String credentialId,
        String publicKey,
        long signCount,
        String deviceInfo,
        String authenticatorData,
        String clientDataJSON,
        String signature
) {
        @Builder
        public PasskeyRequestDTO {}
}
