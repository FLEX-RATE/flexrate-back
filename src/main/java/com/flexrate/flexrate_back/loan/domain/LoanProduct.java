package com.flexrate.flexrate_back.loan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "LoanProduct")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false,length = 255)
    private String description;

    @Column(nullable = false)
    private double maxAmount;

    @Column(nullable = false)
    private double minRate;

    @Column(nullable = false)
    private double maxRate;

    @Column(length = 255)
    private String terms;

    @OneToMany(mappedBy = "loanProduct")
    private List<Interest> Interests;
}
