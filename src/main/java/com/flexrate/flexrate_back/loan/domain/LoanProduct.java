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

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false,length = 255)
    private String description;

    @Column(nullable = false)
    private int maxAmount;

    @Column(nullable = false)
    private float minRate;

    @Column(nullable = false)
    private float maxRate;

    @Column(nullable = false)
    private int terms;
}
