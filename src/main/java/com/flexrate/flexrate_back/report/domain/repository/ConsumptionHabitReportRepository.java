package com.flexrate.flexrate_back.report.domain.repository;

import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.report.domain.ConsumptionHabitReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface ConsumptionHabitReportRepository extends JpaRepository<ConsumptionHabitReport, Long> {
    Optional<ConsumptionHabitReport> findByMemberAndReportMonth(Member member, YearMonth reportMonth);
    List<ConsumptionHabitReport> findAllByMember(Member member);
}
