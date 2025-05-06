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
        Long memberId,
        String accessToken,
        String refreshToken,
        String email,
        String name,
        Sex sex,
        ConsumptionType consumptionType,
        ConsumeGoal consumeGoal,
        List<String> passkeys
) {}
