package com.flexrate.flexrate_back.report.api;

import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.report.application.ConsumptionHabitReportService;
import com.flexrate.flexrate_back.report.domain.ConsumptionHabitReport;
import com.flexrate.flexrate_back.report.dto.ConsumptionHabitReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ConsumptionHabitReportController {

    private final ConsumptionHabitReportService reportService;
    private final MemberService memberService;

    /**
     * 사용자 본인의 소비 습관 리포트 조회 API
     * @param principal 현재 로그인한 사용자 정보
     * @param month 조회할 리포트 월 (yyyy-MM 형식, optional)
     * @return 소비습관 리포트 목록 (단일 or 전체)
     * @since 2025.05.08
     * @author 서채연
     */
    @Operation(
            summary = "소비 습관 개선 리포트 조회",
            description = "특정 월을 입력하면 해당 월 리포트를, 입력하지 않으면 전체 리포트를 반환합니다.",
            parameters = {
                    @Parameter(name = "month", description = "조회할 연월 (yyyy-MM)", required = false, example = "2025-05")
            }
    )
    @GetMapping("/consumption-report")
    public List<ConsumptionHabitReportResponse> getMyReports(
            @RequestParam(value = "month", required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            Principal principal
    ) {
        Long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);

        if (month != null) {
            ConsumptionHabitReport report = reportService.getReport(member, month)
                    .orElseGet(() -> reportService.createReport(member, month, null));

            return List.of(ConsumptionHabitReportResponse.from(report));

        } else {
            return reportService.getAllReportsByMember(member).stream()
                    .map(ConsumptionHabitReportResponse::from)
                    .toList();
        }
    }
}