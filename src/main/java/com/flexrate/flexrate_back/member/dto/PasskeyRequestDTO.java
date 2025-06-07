package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

@Builder
public record PasskeyRequestDTO(
        String credentialKey,
        String clientDataJSON,
        String publicKey,
        int signCount,
        String deviceInfo,
        String authenticatorData,
        String signature
) {}
