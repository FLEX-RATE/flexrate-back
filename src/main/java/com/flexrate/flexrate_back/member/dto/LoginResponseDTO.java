package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

import java.util.List;

/*
 * 로그인 응답
 * @since 2025.05.05
 * @author 윤영찬
 */

@Builder
public record LoginResponseDTO (
        Long memberId,
        String email,
        String accessToken,
        String refreshToken,
        boolean requirePasskeyAuth,
        List<PasskeyDTO> registeredPasskeys
) {}
