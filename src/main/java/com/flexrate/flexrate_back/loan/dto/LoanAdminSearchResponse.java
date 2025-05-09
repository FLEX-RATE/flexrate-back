package com.flexrate.flexrate_back.loan.dto;

import com.flexrate.flexrate_back.common.dto.PaginationInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record LoanAdminSearchResponse(
        PaginationInfo paginationInfo,
        List<LoanAdminSearchSummaryDto> loans
) {}
