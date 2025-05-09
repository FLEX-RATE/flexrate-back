package com.flexrate.flexrate_back.financialdata.domain;

import com.flexrate.flexrate_back.financialdata.enums.UserFinancialCategory;
import com.flexrate.flexrate_back.financialdata.enums.UserFinancialDataType;
import com.flexrate.flexrate_back.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "UserFinancialData")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFinancialData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dataId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserFinancialDataType dataType;

    @Enumerated(EnumType.STRING)
    @Column
    private UserFinancialCategory category;

    @Column(nullable = false, precision = 12)
    private int value;

    @Column(nullable = false)
    private LocalDateTime collectedAt;
}
