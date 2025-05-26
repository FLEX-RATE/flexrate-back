package com.flexrate.flexrate_back.loan.dto;

import com.flexrate.flexrate_back.loan.enums.EmploymentType;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.loan.enums.LoanPurpose;
import com.flexrate.flexrate_back.loan.enums.ResidenceType;
import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record LoanReviewDetailResponse(
        Long applicationId,                         // 대출 신청 ID - LoanApplication
        String applicantName,                       // 신청자 - Member
        LoanApplicationStatus applicationStatus,    // 현재 상태 - LoanApplication
        ConsumptionType consumptionType,            // 소비 성향 - Member
        ConsumeGoal consumeGoal,                    // 소비 목표 - Member

        // === 대출 심사 결과 ===
        LocalDateTime appliedAt,                    // 대출 신청 일자 - LoanApplication
        float interestRateMax,                      // 금리 범위 (끝) - LoanProduct
        float interestRateMin,                      // 금리 범위 (시작) - LoanProduct
        float initialInterestRate,                  // 대출 초기 금리 - Interest
        float lastInterestRate,                     // 최근 금리 - Interest
        LocalDate lastInterestDate,                 // 최근 금리 최종 갱신 일자 - Interest
        int approvedMaxAmount,                      // 대출 가능 한도 - LoanProduct
        int requestedAmount,                        // 요청 대출 금액 - LoanApplication
        LocalDateTime repaymentStartDate,           // 요청 상환 기간 시작일 - LoanApplication
        LocalDateTime repaymentEndDate,             // 요청 상환 기간 종료일 - LoanApplication
        int repaymentMonths,                        // 요청 상환 기간 개월(요청 상환 기간 종료일 - 시작일)

        // === 대출 신청 정보 ===
        EmploymentType employmentType,              // 고용 형태 - LoanReviewHistory
        Integer annualIncome,                       // 연소득 - LoanReviewHistory
        ResidenceType residenceType,                // 주거 형태 - LoanReviewHistory
        Boolean isBankrupt,                         // 개인회생 여부 - LoanReviewHistory
        LoanPurpose loanPurpose                     // 대출 목적 - LoanReviewHistory
) {}
