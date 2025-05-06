package com.flexrate.flexrate_back.loan.dto;

public record LoanProductSummaryDto(
        Long productId,
        String name,
        String description,
        double maxAmount,
        double minRate,
        double maxRate,
        int terms
) {}
