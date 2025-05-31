package com.flexrate.flexrate_back.loan.domain;

import com.flexrate.flexrate_back.loan.enums.EmploymentType;
import com.flexrate.flexrate_back.loan.enums.LoanPurpose;
import com.flexrate.flexrate_back.loan.enums.ResidenceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "LoanReviewHistory")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanReviewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @OneToOne
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;    // 대출 신청서

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;  // 고용형태
    @Enumerated(EnumType.STRING)
    private ResidenceType residenceType;    // 주거형태
    @Enumerated(EnumType.STRING)
    private LoanPurpose loanPurpose;        // 대출목적

    private Integer annualIncome;           // 연소득
    private Boolean isBankrupt;             // 개인회생 여부

    // 대출 심사 결과 갱신
    public void updateReview(EmploymentType employmentType, ResidenceType residenceType,
                             LoanPurpose loanPurpose, Integer annualIncome, Boolean isBankrupt) {
        this.employmentType = employmentType;
        this.residenceType = residenceType;
        this.loanPurpose = loanPurpose;
        this.annualIncome = annualIncome;
        this.isBankrupt = isBankrupt;
    }
}
