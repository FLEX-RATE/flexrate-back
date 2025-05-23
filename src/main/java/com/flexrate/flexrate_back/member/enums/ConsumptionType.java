package com.flexrate.flexrate_back.member.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConsumptionType {

    CONSERVATIVE("절약형", "필요한 것만 소비하는 편이에요"),
    PRACTICAL("균형형", "계획적으로 소비해요."),
    BALANCED("실용형", "필요한 건 아끼지 않고 써요."),
    CONSUMPTION_ORIENTED("소비지향형", "하고 싶은 건 하는 편이에요.");

    private final String name;
    private final String description;
}
