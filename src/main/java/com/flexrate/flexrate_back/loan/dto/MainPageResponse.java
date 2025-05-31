package com.flexrate.flexrate_back.loan.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record MainPageResponse(
        Integer monthlyPayment,
        Integer repaymentRate,
        Integer monthlyPrincipal,
        Integer monthlyInterest,
        LocalDate nextPaymentDate,
        Integer loanRepaymentTransactionNum,
        LocalDateTime recentRepaymentDate,
        LocalDate startDate,
        Integer interestChangedNum,
        Integer totalAmount,
        Integer repaymentMonth
) {
}
