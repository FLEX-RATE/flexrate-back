package com.flexrate.flexrate_back.loan.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record LoanAdminSearchSummaryDto(
        Long id,
        String status,
        LocalDate appliedAt,
        String applicant,
        Long applicantId,
        Integer availableLimit,
        Float initialRate,
        Integer prevLoanCount,
        String type
) {}
