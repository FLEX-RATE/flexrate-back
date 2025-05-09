package com.flexrate.flexrate_back.report.dto;

import java.time.YearMonth;
import java.util.List;

public record ConsumptionCategoryStatsResponse(
        Long memberId,
        YearMonth month,
        List<ConsumptionCategoryRatioResponse> stats
) {}
