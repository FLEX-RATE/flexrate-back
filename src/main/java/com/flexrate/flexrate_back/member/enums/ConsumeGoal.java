package com.flexrate.flexrate_back.member.enums;

import lombok.Getter;

@Getter
public enum ConsumeGoal {
    WEEKLY_BUDGET_FOR_FOOD("식비를 일주일 단위로 예산화해 지출 기록하기");

    private final String description;

    ConsumeGoal(String description) {
        this.description = description;
    }

}
