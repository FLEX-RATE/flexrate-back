package com.flexrate.flexrate_back.loan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPreApplicationRequest {

    @NotNull
    private String businessType;

    @NotNull
    private String employmentType;

    @NotNull
    private String hireDate;

    @NotNull
    private String schoolName;

    @NotNull
    private String educationStatus;

    @NotNull
    private Integer annualIncome;

    @NotNull
    private String creditGrade;

    @NotNull
    private String residenceType;

    @NotNull
    private Boolean isBankrupt;

    @NotNull
    private String loanPurpose;
}
