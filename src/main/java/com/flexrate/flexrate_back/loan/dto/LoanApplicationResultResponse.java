package com.flexrate.flexrate_back.loan.dto;

import lombok.Builder;

@Builder
public record LoanApplicationResultResponse(
        String loanApplicationResult,
        int loanApplicationAmount,
        float loanInterestRate,
        String loanStartDate,
        String loanEndDate
) {}
