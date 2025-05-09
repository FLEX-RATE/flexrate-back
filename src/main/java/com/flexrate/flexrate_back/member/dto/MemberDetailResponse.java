package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;

@Builder
public record MemberDetailResponse(
        Long memberId,
        String name,
        String sex,
        String status,
        String birthDate,
        String createdAt,
        Boolean hasLoan,
        Integer loanTransactionCount,
        String consumptionType,
        String consumeGoal,
        Float interestRate,
        Integer creditScore,
        String loanStartDate,
        String loanEndDate,
        Integer loanAmount,
        Integer totalPayment,
        Integer repaymentDay,
        Integer monthlyPayment
) {}