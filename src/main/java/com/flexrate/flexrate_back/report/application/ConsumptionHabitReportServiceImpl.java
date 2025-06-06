package com.flexrate.flexrate_back.report.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.financialdata.application.UserFinancialDataService;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.report.client.ConsumptionReportApiClient;
import com.flexrate.flexrate_back.report.domain.ConsumptionHabitReport;
import com.flexrate.flexrate_back.report.domain.repository.ConsumptionHabitReportRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumptionHabitReportServiceImpl implements ConsumptionHabitReportService {

    private final ConsumptionHabitReportRepository reportRepository;
    private final ConsumptionReportApiClient apiClient;
    private final MemberService memberService;
    private final UserFinancialDataService userFinancialDataService;

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
    public ConsumptionHabitReport createReport(Member member, YearMonth reportMonth, String summary) {
        if (reportRepository.findByMemberAndReportMonth(member, reportMonth).isPresent()) {
            log.warn("이미 존재하는 소비 개선 리포트 생성 시도 :\nreportMonth={}", reportMonth);
            throw new FlexrateException(ErrorCode.REPORT_ALREADY_EXISTS);
        }

        if (summary == null || summary.isBlank()) {
            log.info("소비 개선 리포트 생성 :\nreportMonth={}", reportMonth);
            summary = apiClient.createConsumptionSummary(member.getMemberId(), reportMonth);
        }

        ConsumptionHabitReport report = ConsumptionHabitReport.builder()
                .member(member)
                .reportMonth(reportMonth)
                .summary(summary)
                .build();
        log.info("소비 개선 리포트 생성 성공 :\nreportMonth={}", reportMonth);

        return reportRepository.save(report);
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
        log.info("특정 소비 개선 리포트 조회 :\nreportMonth={}", reportMonth);
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
        log.info("소비 개선 리포트 목록 조회");
        return reportRepository.findAllByMember(member);
    }

    /**
     * 보고서 생성 가능한 연월 목록 조회 API
     * @param principal 인증된 사용자 정보
     * @return 연월 목록 (예: 2025-05 등)
     */
    @Operation(
            summary = "보고서 생성 가능한 연월 목록 조회",
            description = "회원의 금융 데이터를 기반으로 보고서를 생성할 수 있는 연월 목록을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 연월 목록 반환",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = YearMonth.class))))
            }
    )
    @GetMapping("/available-months")
    public ResponseEntity<List<YearMonth>> getAvailableReportMonths(Principal principal) {
        Member member = memberService.findById(Long.parseLong(principal.getName()));
        List<YearMonth> availableMonths = userFinancialDataService.getReportAvailableMonths(member);
        return ResponseEntity.ok(availableMonths);
    }

}
