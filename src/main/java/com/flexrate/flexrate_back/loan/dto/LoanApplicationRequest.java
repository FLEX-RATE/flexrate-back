package com.flexrate.flexrate_back.loan.dto;


import jakarta.validation.constraints.NotNull;

public record LoanApplicationRequest(
        @NotNull int loanAmount,
        @NotNull int repaymentMonth
) {}
