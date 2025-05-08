package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import lombok.Builder;

@Builder
public record MypageResponse(
        String name,
        String email,
        ConsumeGoal consumeGoal,
        ConsumptionType consumptionType
) {}
