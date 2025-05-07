package com.flexrate.flexrate_back.loan.dto;

public record InterestResponse(
        float currentRate,
        float previousRate,
        float changeRatePercent
) {}
