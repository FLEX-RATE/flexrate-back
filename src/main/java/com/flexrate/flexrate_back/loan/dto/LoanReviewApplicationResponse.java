package com.flexrate.flexrate_back.loan.dto;

import lombok.Builder;

@Builder
public record LoanReviewApplicationResponse(
        String name,
        String screeningDate,
        int loanLimit,
        float initialRate,
        float rateRangeFrom,
        float rateRangeTo,
        int creditScore
) {}
