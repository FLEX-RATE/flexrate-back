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
                .appliedAt(loan.getAppliedAt() == null ? null : loan.getAppliedAt().toLocalDate())
                .applicant(loan.getMember().getName())
                .applicantId(loan.getMember().getMemberId())
                .availableLimit(loan.getRemainAmount())
                .initialRate(loan.getRate())
                .prevLoanCount(loan.getLoanTransactions().size())
                .type(loan.getLoanType() != null ? loan.getLoanType().name() : null)
                .build();
    }

    public LoanReviewDetailResponse toLoanReviewDetailResponse(LoanApplication loan, Interest interest) {
        final var member = loan.getMember();
        final var product = loan.getProduct();
        final var reviewHistory = loan.getReviewHistory();
        final var startDate = loan.getStartDate();
        final var endDate = loan.getEndDate();

        return LoanReviewDetailResponse.builder()
                .applicationId(loan.getApplicationId())
                .applicantName(member != null ? member.getName() : null)
                .applicationStatus(loan.getStatus())
                .consumptionType(member != null ? member.getConsumptionType().getName() : null)
                .consumeGoal(member != null ? member.getConsumeGoal().getDescription() : null)
                .appliedAt(loan.getAppliedAt() != null ? loan.getAppliedAt() : null)
                .approvedMaxAmount(product != null ? product.getMaxAmount() : null)
                .interestRateMax(product != null ? product.getMaxRate() : null)
                .interestRateMin(product != null ? product.getMinRate() : null)
                .initialInterestRate(loan.getRate())
                .lastInterestRate(interest != null ? interest.getInterestRate() : 0.0f)
                .lastInterestDate(interest != null ? interest.getInterestDate() : null)
                .requestedAmount(loan.getTotalAmount())
                .repaymentStartDate(startDate)
                .repaymentEndDate(endDate)
                .repaymentMonths(
                        (startDate != null && endDate != null) ?
                                (int) java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate) : 0
                )
                .employmentType(reviewHistory != null ? reviewHistory.getEmploymentType() : null)
                .annualIncome(reviewHistory != null ? reviewHistory.getAnnualIncome() : null)
                .residenceType(reviewHistory != null ? reviewHistory.getResidenceType() : null)
                .isBankrupt(reviewHistory != null ? reviewHistory.getIsBankrupt() : null)
                .loanPurpose(reviewHistory != null ? reviewHistory.getLoanPurpose() : null)
                .build();
    }
}
