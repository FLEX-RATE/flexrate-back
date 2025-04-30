package com.flexrate.flexrate_back.loan.dto;

import com.flexrate.flexrate_back.loan.enums.TransactionStatus;
import com.flexrate.flexrate_back.loan.enums.TransactionType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TransactionHistorySummaryDto (
        Long transactionId,
        Long applicationId,
        Long memberId,
        TransactionType type,
        double amount,
        LocalDateTime occurredAt,
        TransactionStatus status
) {}