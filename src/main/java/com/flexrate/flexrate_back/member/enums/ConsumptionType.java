package com.flexrate.flexrate_back.member.enums;

import lombok.Getter;

@Getter
public enum ConsumptionType {
    SAVING("절약형"),
    PRACTICAL("실용형"),
    BALANCE("균형형"),
    CONSUMER_ORIENTED("일반형");

    private final String label;

    ConsumptionType(String label) {
        this.label = label;
    }

    public static ConsumptionType from(String input) {
        for (ConsumptionType type : values()) {
            if (type.label.equals(input)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown consumption type: " + input);
    }
}
