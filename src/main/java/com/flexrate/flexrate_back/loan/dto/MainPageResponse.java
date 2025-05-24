package com.flexrate.flexrate_back.loan.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record MainPageResponse(
        Integer monthlyPayment,
        Integer repaymentRate,
        Integer monthlyPrincipal,
        Integer monthlyInterest,
        LocalDate nextPaymentDate,
        Integer loanRepaymentTransactionNum,
        LocalDate startDate,
        Integer intersetChangedNum
) {
}
