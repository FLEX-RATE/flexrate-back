package com.flexrate.flexrate_back.loan.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LoanApplicationRequest(
        @NotNull int loanAmount,
        @NotNull int repaymentMonth
) {}
