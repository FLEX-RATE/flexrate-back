package com.flexrate.flexrate_back.loan.dto;

import com.flexrate.flexrate_back.loan.enums.EmploymentType;
import com.flexrate.flexrate_back.loan.enums.LoanPurpose;
import com.flexrate.flexrate_back.loan.enums.ResidenceType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LoanReviewApplicationRequest(

        @NotNull(message = "고용형태(employmentType)은 필수 입력 항목입니다.")
        EmploymentType employmentType,

        @NotNull(message = "연소득(annualIncome)은 필수 입력 항목입니다.")
        Integer annualIncome,

        @NotNull(message = "주거 형태(residenceType)는 필수 입력 항목입니다.")
        ResidenceType residenceType,

        @NotNull(message = "개인회생 여부(isBankrupt)는 필수 입력 항목입니다.")
        Boolean isBankrupt,

        @NotNull(message = "대출 목적(loanPurpose)은 필수 입력 항목입니다.")
        LoanPurpose loanPurpose
) {}
