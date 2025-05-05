package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/*
 * 패스키 정보 DTO
 * @since 2025.05.06
 * @author 윤영찬
 */
@Builder
public record PasskeyDTO(
        Long credentialId,
        String deviceInfo,
        LocalDateTime lastUsedDate
) {}
