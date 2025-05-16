package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

/**
 * 패스키 로그인 인증 요청 DTO
 * @since 2025.05.11
 */
@Builder
public record PasskeyAuthenticationDTO(
        String credentialId,
        String authenticatorData,
        String clientDataJSON,
        String signature
) {}