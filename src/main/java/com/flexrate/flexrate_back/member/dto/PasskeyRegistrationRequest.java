package com.flexrate.flexrate_back.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PasskeyRegistrationRequest(
        String credentialId,
        String rawId,
        String clientDataJSON,
        String attestationObject,
        String deviceInfo,
        String publicKey,
        int signCount,
        String authenticatorData,
        String signature
) {}