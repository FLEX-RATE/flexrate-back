package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

@Builder
public record PasskeyRegistrationRequest(
        String credentialKey,
        String attestationObject,
        String clientDataJSON,
        String deviceInfo
) {
    @Builder
    public PasskeyRegistrationRequest {}
}
