package com.flexrate.flexrate_back.report.api;

import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.report.application.ReportStatisticsService;
import com.flexrate.flexrate_back.report.dto.ConsumptionCategoryStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class ReportStatisticsController {

    private final ReportStatisticsService statisticsService;
    private final MemberService memberService;

    @Operation(summary = "카테고리별 소비 통계 조회", description = "해당 월의 소비 통계를 카테고리 기준으로 집계하여 반환합니다.")
    @GetMapping("/consumption-statistic")
    public ConsumptionCategoryStatsResponse getMyStats(
            Principal principal,
            @Parameter(description = "조회할 연월 (yyyy-MM)", example = "2025-05")
            @RequestParam("조회할 연월 (yyyy-MM)")
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        Long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);

        return statisticsService.getCategoryStats(member, month);
    }
}
