package com.parkit.parkingsystem.util;

import com.parkit.parkingsystem.constants.Fare;

public class PriceUtil {

    /**
     * return rounded price to Fare.ROUNDING_PRECISION
     *
     * @param price to be rounded
     * @return rounded price
     */
    public static double getRoundedPrice(double price) {
        return Math.round(price * Fare.ROUNDING_PRECISION) / Fare.ROUNDING_PRECISION;
    }
}
