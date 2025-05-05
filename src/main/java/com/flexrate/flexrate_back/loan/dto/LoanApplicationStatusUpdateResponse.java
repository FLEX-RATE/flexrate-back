package com.flexrate.flexrate_back.loan.dto;

import lombok.Builder;

@Builder
public record LoanApplicationStatusUpdateResponse(
        Long loanApplicationId,
        boolean success,
        String message
) {}