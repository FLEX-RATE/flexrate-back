package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

@Builder
public record PasskeyAuthenticationDTO(
        String credentialId,
        String authenticatorData,
        String clientDataJSON,
        String signature
) {}