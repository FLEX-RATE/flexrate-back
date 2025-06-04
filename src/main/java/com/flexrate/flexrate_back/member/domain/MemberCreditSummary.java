package com.flexrate.flexrate_back.member.domain;

import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.domain.LoanTransaction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "MemberCreditSummary")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCreditSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication loanApplication;

    @CreatedDate
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "total_loan_count", nullable = false)
    @Builder.Default
    private Integer totalLoanCount = 0;

    @Column(name = "active_loan_count", nullable = false)
    @Builder.Default
    private Integer activeLoanCount = 0;

    @Column(name = "total_loan_balance", nullable = false)
    @Builder.Default
    private Integer totalLoanBalance = 0;

    @Column(name = "total_loan_overdue_30d", nullable = false)
    @Builder.Default
    private Integer totalLoanOverdue30d = 0;

    @Column(name = "total_loan_overdue_90d", nullable = false)
    @Builder.Default
    private Integer totalLoanOverdue90d = 0;

    @Column(name = "has_current_overdue", nullable = false)
    @Builder.Default
    private Boolean hasCurrentOverdue = false;

    @Column(name = "last_overdue_date")
    @Temporal(TemporalType.DATE)
    private LocalDate lastOverdueDate;

    @Column(name = "comm_overdue_count", nullable = false)
    @Builder.Default
    private Integer commOverdueCount = 0;

    @Column(name = "comm_overdue_max_days", nullable = false)
    @Builder.Default
    private Integer commOverdueMaxDays = 0;

    @Column(name = "utility_overdue_count", nullable = false)
    @Builder.Default
    private Integer utilityOverdueCount = 0;

    @Column(name = "utility_overdue_max_days", nullable = false)
    @Builder.Default
    private Integer utilityOverdueMaxDays = 0;

    @Column(name = "credit_score", nullable = false)
    private Integer creditScore;

    @Column(name = "remark", length = 255)
    private String remark;
}
