package com.flexrate.flexrate_back.loan.domain;

import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "LoanApplication")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private LocalDateTime appliedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanApplicationStatus status;

    private LocalDateTime executedAt;
    private double amount;
    private double rate;

    @Column(length = 255)
    private String reviewResult;

    @OneToMany(mappedBy = "application")
    private List<LoanTransaction> loanTransactions;

    private double creditScore;
}
