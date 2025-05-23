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

    /**
     * 매월 1일 자정에 실행되는 소비습관 리포트 자동 생성 스케줄러
     * @since 2025.05.08
     * @author 서채연
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void generateMonthlyReports() {
        YearMonth targetMonth = YearMonth.now().minusMonths(1);
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            String summary = apiClient.createConsumptionSummary(member.getMemberId(), targetMonth);
            reportService.createReport(member, targetMonth, summary);
        }
    }
}
