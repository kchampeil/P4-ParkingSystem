package com.parkit.parkingsystem.util;

import com.parkit.parkingsystem.constants.Fare;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceUtil {

    /**
     * return rounded price to Fare.ROUNDING_PRECISION
     *
     * @param price to be rounded
     * @return rounded price
     */
    public static double getRoundedPrice(double price) {
        BigDecimal roundedPrice = new BigDecimal(String.valueOf(price)).setScale(Fare.ROUNDING_PRECISION, RoundingMode.HALF_UP);
        return roundedPrice.doubleValue();
    }
}
