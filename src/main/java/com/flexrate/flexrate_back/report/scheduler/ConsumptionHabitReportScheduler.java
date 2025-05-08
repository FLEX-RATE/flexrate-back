package com.flexrate.flexrate_back.report.scheduler;

import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.report.application.ConsumptionHabitReportService;
import com.flexrate.flexrate_back.report.client.ConsumptionReportApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumptionHabitReportScheduler {

    private final MemberRepository memberRepository;
    private final ConsumptionHabitReportService reportService;
    private final ConsumptionReportApiClient apiClient;

    @Scheduled(cron = "0 0 0 1 * ?")
    public void generateMonthlyReports() {
        YearMonth targetMonth = YearMonth.now().minusMonths(1);
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            try {
                String summary = apiClient.getConsumptionSummary(member.getMemberId(), targetMonth);
                reportService.createReport(member, targetMonth, summary);
                log.info("[ReportScheduler] {}월 리포트 생성 완료 - {}", targetMonth, member.getEmail());
            } catch (Exception e) {
                log.warn("[ReportScheduler] {}월 리포트 생성 실패 - {}: {}", targetMonth, member.getEmail(), e.getMessage());
            }
        }
    }
}
