package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import lombok.Builder;

@Builder
public record AnalyzeConsumptionTypeResponse(
        ConsumptionType consumptionType
) {
}
