package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ConversionConstants;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.time.Duration;

import static com.parkit.parkingsystem.constants.Fare.MINUTES_BEFORE_PAYABLE_PARKING_TIME;

public class FareCalculatorService {

    /**
     * calculate parking fare for one given ticket.
     *
     * @param ticket one initialized ticket
     */
    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        //Calculate the duration between enter and exit
        Duration durationBetweenInAndOut = Duration.between(ticket.getInTime().toInstant(),
                ticket.getOutTime().toInstant());
        // then convert it in hours (in double type)
        double durationInHours = (double) durationBetweenInAndOut.toMinutes() / ConversionConstants.MINUTES_TO_HOUR_DIVIDER;

        // apply the free park advantage if duration is under the limit defined in MINUTES_BEFORE_PAYABLE_PARKING_TIME
        if (durationBetweenInAndOut.toMinutes() <= MINUTES_BEFORE_PAYABLE_PARKING_TIME) {
            durationInHours = 0;
        }

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(durationInHours * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(durationInHours * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }

        // apply the discount PERCENTAGE_OF_DISCOUNT_FOR_RECURRING_USER if recurrent user
        if (ticket.getWithDiscount()) {
            double discountedPrice = ticket.getPrice()
                    * (1 - (Fare.PERCENTAGE_OF_DISCOUNT_FOR_RECURRING_USER / ConversionConstants.VALUE_TO_PERCENT_DIVIDER));
            ticket.setPrice(discountedPrice);
        }
    }
}