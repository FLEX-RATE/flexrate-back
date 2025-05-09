package com.flexrate.flexrate_back.loan.mapper;

import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.dto.LoanAdminSearchSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class LoanApplicationMapper {

    public LoanAdminSearchSummaryDto toSummaryDto(LoanApplication loan) {
        return LoanAdminSearchSummaryDto.builder()
                .id(loan.getApplicationId())
                .status(loan.getStatus().name())
                .appliedAt(loan.getAppliedAt().toLocalDate())
                .applicant(loan.getMember().getName())
                .applicantId(loan.getMember().getMemberId())
                .availableLimit(loan.getRemainAmount())
                .initialRate(loan.getRate())
                .prevLoanCount(loan.getLoanTransactions().size())
                .type(loan.getLoanType() != null ? loan.getLoanType().name() : null)
                .build();
    }
}
