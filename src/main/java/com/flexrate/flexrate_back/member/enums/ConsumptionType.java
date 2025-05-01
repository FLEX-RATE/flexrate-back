package com.flexrate.flexrate_back.member.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 소비 성향을 나타내는 Enum
 * @since 2025.05-01
 * @Author 윤영찬
 */
public enum ConsumptionType {
    SAVING("절약형"),
    BALANCE("균형형"),
    PRACTICAL("실용형"),
    CONSUMER_ORIENTED("소비지향형");

    private final String label;

    ConsumptionType(String label) {
        this.label = label;
    }


    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static ConsumptionType fromLabel(String label) {
        for (ConsumptionType v : values()) {
            if (v.label.equals(label)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown consumptionType: " + label);
    }

    public static boolean isValidLabel(String label) {
        if (label == null) return false;
        for (ConsumptionType v : values()) {
            if (v.label.equals(label)) {
                return true;
            }
        }
        return false;
    }
}
