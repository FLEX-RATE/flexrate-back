package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import lombok.Builder;

import java.util.List;

@Builder
public record ConsumeGoalResponse (
        List<ConsumeGoalSummary> consumeGoals
) {
    @Builder
    public record ConsumeGoalSummary(
            ConsumeGoal consumeGoal,
            String description
    ) {}
}
