package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

/*
 * 패스키 정보 전달 DTO
 * @since 2025.05.07
 * @author 윤영찬
 */
@Builder
public record PasskeyRequestDTO(
        Long credentialId,
        String publicKey,
        int signCount,
        String deviceInfo,
        boolean isActive
) {}
