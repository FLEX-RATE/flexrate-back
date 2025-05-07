package com.flexrate.flexrate_back.loan.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Interest")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interestId;

    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication loanApplication;

    @NotNull
    private float interestRate;

    @NotNull
    private LocalDateTime interestDate;

}
