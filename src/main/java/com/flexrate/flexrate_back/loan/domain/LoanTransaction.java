package com.flexrate.flexrate_back.loan.domain;

import com.flexrate.flexrate_back.loan.enums.TransactionType;
import com.flexrate.flexrate_back.loan.enums.TransactionStatus;
import com.flexrate.flexrate_back.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "LoanTransaction")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 12)
    private double amount;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
}
