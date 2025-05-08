package com.flexrate.flexrate_back.report.dto;

public record ConsumptionCategoryRatioResponse(
        String category,
        int amount,
        double percentage
) {}
