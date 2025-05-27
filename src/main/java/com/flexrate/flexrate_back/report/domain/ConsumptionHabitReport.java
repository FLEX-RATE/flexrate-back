package com.flexrate.flexrate_back.report.domain;

import com.flexrate.flexrate_back.common.converter.YearMonthAttributeConverter;
import com.flexrate.flexrate_back.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Entity
@Table(
        name = "ConsumptionHabitReport",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "report_month"})
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumptionHabitReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Convert(converter = YearMonthAttributeConverter.class)
    @Column(name = "report_month", nullable = false, length = 7)
    private YearMonth reportMonth;

    @Column(length = 500)
    private String summary;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    // 소비 카테고리별 통계 정보
    @Column(name = "consumptions", columnDefinition = "TEXT")
    private String consumptions;

    public void setConsumptions(String consumptions) {
        this.consumptions = consumptions;
    }

}
