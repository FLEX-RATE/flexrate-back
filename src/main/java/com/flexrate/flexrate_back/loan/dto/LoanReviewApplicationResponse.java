package com.flexrate.flexrate_back.loan.dto;

public record LoanReviewApplicationResponse(
        String name,
        String screeningDate,
        int loanLimit,
        double initialRate,
        float rateRangeFrom,
        float rateRangeTo,
        int creditScore
) {}
