package com.flexrate.flexrate_back.loan.dto;

import com.flexrate.flexrate_back.loan.enums.PeriodType;

public record InterestAnalysisRequest(Long applicationId, PeriodType periodType) {}
