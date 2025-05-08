package com.flexrate.flexrate_back.report.api;

import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.report.application.ConsumptionHabitReportService;
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

    @Operation(summary = "소비 리포트 조회", description = "특정 월을 입력하면 해당 월 리포트를, 입력하지 않으면 전체 리포트를 반환합니다.")
    @GetMapping("/consumption-report")
    public List<ConsumptionHabitReportResponse> getMyReports(
            Principal principal,
            @Parameter(description = "조회할 연월 (yyyy-MM)", example = "2025-05")
            @RequestParam(value = "조회할 연월 (yyyy-MM)", required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        Long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);

        if (month != null) {
            return reportService.getReport(member, month)
                    .map(ConsumptionHabitReportResponse::from)
                    .map(List::of)
                    .orElse(List.of());
        } else {
            return reportService.getAllReportsByMember(member).stream()
                    .map(ConsumptionHabitReportResponse::from)
                    .toList();
        }
    }

}