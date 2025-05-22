package com.flexrate.flexrate_back.report.domain;

import com.flexrate.flexrate_back.financialdata.enums.UserFinancialCategory;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ConsumptionHabitCategory")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumptionHabitCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private ConsumptionHabitReport report;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserFinancialCategory category;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal ratio;
}
