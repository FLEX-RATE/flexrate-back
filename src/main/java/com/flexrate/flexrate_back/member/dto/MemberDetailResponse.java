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
        Integer totalPayment,
        String paymentDue,
        Integer monthlyPayment
) {}