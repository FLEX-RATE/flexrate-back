package com.flexrate.flexrate_back.common.util;

import java.util.Random;

public class RandomStringUtil {
    private static final int NUMERIC_LEFT = 48;     // 아스키코드 : 0
    private static final int NUMERIC_RIGHT = 57;    // 아스키코드 : 9

    public static String numeric(int length) {
        Random random = new Random();
        return random.ints(NUMERIC_LEFT, NUMERIC_RIGHT + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
