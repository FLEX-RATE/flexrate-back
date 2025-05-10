package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

/*
 * 로그인 응답 PASSKEY 방식일 경우 반환
 * @since 2025.05.05
 * @author 윤영찬
 */

@Builder
public record LoginResponseDTO(
        Long userId,
        String email,
        String accessToken,
        String refreshToken,
        String challenge
) {}