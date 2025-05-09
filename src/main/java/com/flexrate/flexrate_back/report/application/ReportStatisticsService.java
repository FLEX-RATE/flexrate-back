package com.flexrate.flexrate_back.report.application;

import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.report.dto.ConsumptionCategoryRatioResponse;
import com.flexrate.flexrate_back.report.dto.ConsumptionCategoryStatsResponse;
import com.flexrate.flexrate_back.financialdata.domain.repository.UserFinancialDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportStatisticsService {

    private final UserFinancialDataRepository financialDataRepository;

    /**
     * 특정 회원의 지정 월에 대한 카테고리별 소비 통계 조회
     * @param member 조회 대상 회원
     * @param month 조회할 연월 (yyyy-MM)
     * @return 해당 회원의 카테고리별 소비 통계 응답 DTO
     * @since 2025.05.08
     * @author 서채연
     */
    public ConsumptionCategoryStatsResponse getCategoryStats(Member member, YearMonth month) {
        List<ConsumptionCategoryRatioResponse> stats = financialDataRepository.findCategoryStatsWithRatio(member, month);

        return new ConsumptionCategoryStatsResponse(
                member.getMemberId(),
                month,
                stats
        );
    }
}
