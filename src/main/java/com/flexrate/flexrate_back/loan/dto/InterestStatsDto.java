package com.flexrate.flexrate_back.loan.dto;

public record InterestStatsDto(
        String period,
        float averageRate,
        Float changeRatePercent
) {}
