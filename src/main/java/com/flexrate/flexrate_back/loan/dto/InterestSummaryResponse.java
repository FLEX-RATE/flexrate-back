package com.flexrate.flexrate_back.loan.dto;

import java.util.List;

public record InterestSummaryResponse(
        List<InterestStatsDto> rates,
        float highestRate,
        float lowestRate
) {}
