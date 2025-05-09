package com.flexrate.flexrate_back.report.application;

import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.report.domain.ConsumptionHabitReport;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface ConsumptionHabitReportService {

    void createReport(Member member, YearMonth reportMonth, String summary);

    Optional<ConsumptionHabitReport> getReport(Member member, YearMonth reportMonth);

    List<ConsumptionHabitReport> getAllReportsByMember(Member member);
}
