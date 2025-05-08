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

    public ConsumptionCategoryStatsResponse getCategoryStats(Member member, YearMonth month) {
        List<ConsumptionCategoryRatioResponse> stats = financialDataRepository.findCategoryStatsWithRatio(member, month);

        return new ConsumptionCategoryStatsResponse(
                member.getMemberId(),
                month,
                stats
        );
    }
}
