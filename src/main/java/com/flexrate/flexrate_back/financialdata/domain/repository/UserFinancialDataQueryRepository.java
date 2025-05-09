package com.flexrate.flexrate_back.financialdata.domain.repository;

import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.report.dto.ConsumptionCategoryRatioResponse;

import java.time.YearMonth;
import java.util.List;

public interface UserFinancialDataQueryRepository {
    List<ConsumptionCategoryRatioResponse> findCategoryStatsWithRatio(Member member, YearMonth month);
}
