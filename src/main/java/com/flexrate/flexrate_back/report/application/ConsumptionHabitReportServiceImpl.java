package com.flexrate.flexrate_back.report.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
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

    /**
     * 특정 회원의 특정 월 소비습관 리포트를 생성
     * @param member 리포트를 생성할 대상 회원
     * @param reportMonth 생성할 리포트의 대상 월 (yyyy-MM)
     * @param summary 소비 요약 내용 (null이면 외부 API 요청)
     * @throws FlexrateException REPORT_ALREADY_EXISTS 중복된 리포트가 이미 존재하는 경우
     * @since 2025.05.08
     * @author 서채연
     */
    @Override
    public void createReport(Member member, YearMonth reportMonth, String summary) {
        if (reportRepository.findByMemberAndReportMonth(member, reportMonth).isPresent()) {
            throw new FlexrateException(ErrorCode.REPORT_ALREADY_EXISTS);
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

    /**
     * 특정 회원의 특정 월에 해당하는 소비습관 리포트 조회
     * @param member 조회 대상 회원
     * @param reportMonth 조회할 리포트의 월 (yyyy-MM)
     * @return 해당 월 리포트가 존재할 경우 Optional에 래핑되어 반환, 없으면 빈 Optional
     * @since 2025.05.08
     * @author 서채연
     */
    @Override
    public Optional<ConsumptionHabitReport> getReport(Member member, YearMonth reportMonth) {
        return reportRepository.findByMemberAndReportMonth(member, reportMonth);
    }

    /**
     * 특정 회원이 작성한 모든 소비습관 리포트 목록 조회
     * @param member 조회 대상 회원
     * @return 소비습관 리포트 목록
     * @since 2025.05.08
     * @author 서채연
     */
    @Override
    public List<ConsumptionHabitReport> getAllReportsByMember(Member member) {
        return reportRepository.findAllByMember(member);
    }
}
