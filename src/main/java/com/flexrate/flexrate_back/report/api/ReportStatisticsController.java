package com.flexrate.flexrate_back.report.api;

import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.report.application.ReportStatisticsService;
import com.flexrate.flexrate_back.report.dto.ConsumptionCategoryStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

    /**
     * 사용자 본인의 카테고리별 소비 통계 조회 API
     * @param principal 현재 로그인한 사용자 정보
     * @param month 통계를 조회할 연월 (yyyy-MM 형식)
     * @return 카테고리별 소비 통계 응답 객체
     * @since 2025.05.08
     * @author 서채연
     */
    @Operation(
            summary = "카테고리별 소비 통계 조회",
            description = "해당 월의 소비 통계를 카테고리 기준으로 집계하여 반환합니다.",
            parameters = {
                    @Parameter(
                            name = "month",
                            description = "조회할 연월 (yyyy-MM)",
                            required = true,
                            example = "2025-05"
                    )
            }
    )
    @GetMapping("/consumption-statistic")
    public ConsumptionCategoryStatsResponse getMyStats(
            @RequestParam(value = "month")
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            Principal principal
    ) {
        Long memberId = Long.parseLong(principal.getName());
        Member member = memberService.findById(memberId);

        return statisticsService.getCategoryStats(member, month);
    }
}
