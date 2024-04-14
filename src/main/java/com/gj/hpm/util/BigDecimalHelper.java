package com.gj.hpm.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalHelper {

    public static synchronized final String convertBigDecimalToString(BigDecimal data, int scale,
            RoundingMode roundingMode) {

        data = data.setScale(scale, roundingMode);
        return data.toString();
    }
}
