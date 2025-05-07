package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import com.flexrate.flexrate_back.member.enums.Sex;
import lombok.Builder;

import java.util.List;

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
        String passkeyList // 패스키 목록을 포함
) {}