package com.flexrate.flexrate_back.report.dto;

import com.flexrate.flexrate_back.report.domain.ConsumptionHabitReport;

import java.time.LocalDate;
import java.time.YearMonth;

public record ConsumptionHabitReportResponse(
        Long reportId,
        Long memberId,
        YearMonth reportMonth,
        String summary,
        LocalDate createdAt
) {
    public static ConsumptionHabitReportResponse from(ConsumptionHabitReport report) {
        return new ConsumptionHabitReportResponse(
                report.getReportId(),
                report.getMember().getMemberId(),
                report.getReportMonth(),
                report.getSummary(),
                report.getCreatedAt()
        );
    }
}
