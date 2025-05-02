package com.flexrate.flexrate_back.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanReviewApplicationRequest {

    @NotBlank(message = "업종(businessType)은 필수 입력 항목입니다.")
    private String businessType;

    @NotBlank(message = "고용형태(employmentType)은 필수 입력 항목입니다.")
    private String employmentType;

    @NotBlank(message = "입사일자(hireDate)는 필수 입력 항목입니다.")
    private String hireDate;

    @NotBlank(message = "학교명(schoolName)은 필수 입력 항목입니다.")
    private String schoolName;

    @NotBlank(message = "학적 상태(educationStatus)는 필수 입력 항목입니다.")
    private String educationStatus;

    @NotNull(message = "연소득(annualIncome)은 필수 입력 항목입니다.")
    private Integer annualIncome;

    @NotBlank(message = "신용등급(creditGrade)은 필수 입력 항목입니다.")
    private String creditGrade;

    @NotBlank(message = "주거 형태(residenceType)는 필수 입력 항목입니다.")
    private String residenceType;

    @NotNull(message = "개인회생 여부(isBankrupt)는 필수 입력 항목입니다.")
    private Boolean isBankrupt;

    @NotBlank(message = "대출 목적(loanPurpose)은 필수 입력 항목입니다.")
    private String loanPurpose;
}
