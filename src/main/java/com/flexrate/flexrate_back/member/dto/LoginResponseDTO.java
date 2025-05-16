package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

/*
 * 로그인 응답 엑세스토큰과 리프레시토큰 반환
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