package com.flexrate.flexrate_back.loan.mapper;

import com.flexrate.flexrate_back.loan.domain.LoanTransaction;
import com.flexrate.flexrate_back.loan.dto.TransactionHistorySummaryDto;
import org.springframework.stereotype.Component;

@Component
public class LoanTransactionMapper {
    public TransactionHistorySummaryDto toSummaryDto(LoanTransaction loanTransaction) {
        return TransactionHistorySummaryDto.builder()
                .transactionId(loanTransaction.getTransactionId())
                .applicationId(loanTransaction.getApplication().getApplicationId())
                .memberId(loanTransaction.getMember().getMemberId())
                .type(loanTransaction.getType())
                .amount(loanTransaction.getAmount())
                .occurredAt(loanTransaction.getOccurredAt())
                .status(loanTransaction.getStatus())
                .build();
    }
}
