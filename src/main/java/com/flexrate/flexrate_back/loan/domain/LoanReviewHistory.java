package com.flexrate.flexrate_back.loan.domain;

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
    private LoanApplication application;                  // 대출 신청서

    @Column(name = "employment_type", length = 30)
    private String employmentType;                        // 고용형태
    private Integer annualIncome;                         // 연소득

    @Column(name = "residence_type", length = 30)
    private String residenceType;                         // 주거형태
    private Boolean isBankrupt;                           // 개인회생 여부

    @Column(name = "loan_purpose", length = 50)
    private String loanPurpose;                           // 대출목적
}
