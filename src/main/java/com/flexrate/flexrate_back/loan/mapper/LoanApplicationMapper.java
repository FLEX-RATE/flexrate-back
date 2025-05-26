package com.flexrate.flexrate_back.loan.mapper;

import com.flexrate.flexrate_back.loan.domain.Interest;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.dto.LoanAdminSearchSummaryDto;
import com.flexrate.flexrate_back.loan.dto.LoanReviewDetailResponse;
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

    public LoanReviewDetailResponse toLoanReviewDetailResponse(LoanApplication loan, Interest interest) {
        return LoanReviewDetailResponse.builder()
                .applicationId(loan.getApplicationId())
                .applicantName(loan.getMember().getName())
                .applicationStatus(loan.getStatus())
                .consumptionType(loan.getMember().getConsumptionType())
                .consumeGoal(loan.getMember().getConsumeGoal())

                .appliedAt(loan.getAppliedAt())
                .approvedMaxAmount(loan.getProduct().getMaxAmount())
                .interestRateMax(loan.getProduct().getMaxRate())
                .interestRateMin(loan.getProduct().getMinRate())
                .initialInterestRate(loan.getRate())
                .lastInterestRate(interest != null ? interest.getInterestRate() : 0.0f)
                .lastInterestDate(interest != null ? interest.getInterestDate() : null)
                .requestedAmount(loan.getTotalAmount())
                .repaymentStartDate(loan.getStartDate())
                .repaymentEndDate(loan.getEndDate())
                .repaymentMonths(
                        (int) java.time.temporal.ChronoUnit.MONTHS.between(loan.getStartDate(), loan.getEndDate())
                )

                .employmentType(loan.getReviewHistory() != null ? loan.getReviewHistory().getEmploymentType() : null)
                .annualIncome(loan.getReviewHistory() != null ? loan.getReviewHistory().getAnnualIncome() : null)
                .residenceType(loan.getReviewHistory() != null ? loan.getReviewHistory().getResidenceType() : null)
                .isBankrupt(loan.getReviewHistory() != null ? loan.getReviewHistory().getIsBankrupt() : null)
                .loanPurpose(loan.getReviewHistory() != null ? loan.getReviewHistory().getLoanPurpose() : null)
                .build();
    }
}
