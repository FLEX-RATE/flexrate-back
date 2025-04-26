package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.common.dto.PaginationInfo;
import lombok.Builder;
import java.util.List;

@Builder
public record MemberSearchResponse(
        PaginationInfo paginationInfo,
        List<MemberSummaryDto> members
) {}
