package com.flexrate.flexrate_back.report.application;

import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.report.client.ConsumptionReportApiClient;
import com.flexrate.flexrate_back.report.domain.ConsumptionHabitReport;
import com.flexrate.flexrate_back.report.domain.repository.ConsumptionHabitReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConsumptionHabitReportServiceImpl implements ConsumptionHabitReportService {

    private final ConsumptionHabitReportRepository reportRepository;
    private final ConsumptionReportApiClient apiClient;

    @Override
    public void createReport(Member member, YearMonth reportMonth, String summary) {
        if (reportRepository.findByMemberAndReportMonth(member, reportMonth).isPresent()) {
            throw new IllegalStateException("이미 존재");
        }

        if (summary == null || summary.isBlank()) {
             summary = apiClient.getConsumptionSummary(member.getMemberId(), reportMonth);
        }

        reportRepository.save(
                ConsumptionHabitReport.builder()
                        .member(member)
                        .reportMonth(reportMonth)
                        .summary(summary)
                        .build()
        );
    }

    @Override
    public Optional<ConsumptionHabitReport> getReport(Member member, YearMonth reportMonth) {
        return reportRepository.findByMemberAndReportMonth(member, reportMonth);
    }

    @Override
    public List<ConsumptionHabitReport> getAllReportsByMember(Member member) {
        return reportRepository.findAllByMember(member);
    }
}
