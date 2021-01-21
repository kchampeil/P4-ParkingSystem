package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ConversionConstants;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.testconstants.TimeTestConstants;
import com.parkit.parkingsystem.util.PriceUtil;
import org.junit.jupiter.api.*;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private static ParkingSpot parkingSpotCar;
    private static ParkingSpot parkingSpotBike;
    private static ParkingSpot parkingSpotUnknown;
    private static Date outTime;
    private static Date inTime;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
        parkingSpotCar = new ParkingSpot(1, ParkingType.CAR, false);
        parkingSpotBike = new ParkingSpot(1, ParkingType.BIKE, false);
        parkingSpotUnknown = new ParkingSpot(1, null, false);
        outTime = new Date();
        outTime.setTime(System.currentTimeMillis());
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
        inTime = new Date();
        ticket.setOutTime(outTime);
    }

    @Test
    public void calculateFareCar() {
        inTime.setTime(outTime.getTime() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotCar);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    public void calculateFareBike() {
        inTime.setTime(outTime.getTime() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotBike);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    public void calculateFareUnknownType() {
        inTime.setTime(outTime.getTime() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotUnknown);

        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime() {
        inTime.setTime(outTime.getTime() + (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotBike);

        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
        inTime.setTime(outTime.getTime()
                - (45 * TimeTestConstants.MINUTES_TO_MILLISECONDS));
        //45 minutes parking time should give 3/4th parking fare
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotBike);

        fareCalculatorService.calculateFare(ticket);

        double expectedPrice = PriceUtil.getRoundedPrice(0.75 * Fare.BIKE_RATE_PER_HOUR);
        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime() {
        inTime.setTime(outTime.getTime()
                - (45 * TimeTestConstants.MINUTES_TO_MILLISECONDS));
        //45 minutes parking time should give 3/4th parking fare
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotCar);

        fareCalculatorService.calculateFare(ticket);

        double expectedPrice = PriceUtil.getRoundedPrice(0.75 * Fare.CAR_RATE_PER_HOUR);
        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() {
        inTime.setTime(outTime.getTime()
                - (24 * 60 * TimeTestConstants.MINUTES_TO_MILLISECONDS));
        //24 hours parking time should give 24 * parking fare per hour
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotCar);

        fareCalculatorService.calculateFare(ticket);

        double expectedPrice = PriceUtil.getRoundedPrice(24 * Fare.CAR_RATE_PER_HOUR);
        assertEquals(expectedPrice, ticket.getPrice());
    }

    /* ----------------------------------------------------------------------------------------------------------------------
     *                  Free30MinPark tests
     * ----------------------------------------------------------------------------------------------------------------------
     * GIVEN a user staying 5 minutes under the the payable parking limit WHEN calculating the fare
     * THEN we get free park
     *
     * GIVEN a user staying exactly the number of minutes of the payable parking limit WHEN calculating the fare
     * THEN we get free park
     *
     * GIVEN a car user staying 1 minute after the payable parking limit WHEN calculating the fare
     * THEN we get a fare according to the vehicle type and the duration
     * -------------------------------------------------------------------------------------------------------------------- */

    @Test
    @Tag("Free30MinPark")
    @DisplayName("GIVEN a bike user staying 5 minutes under the the payable parking limit WHEN calculating the fare THEN we get free park")
    public void calculateFareBikeWithFiveMinutesUnderFreeLimitParkingTime() {
        inTime.setTime(outTime.getTime()
                - ((Fare.MINUTES_BEFORE_PAYABLE_PARKING_TIME - 5) * TimeTestConstants.MINUTES_TO_MILLISECONDS));
        //5 minutes under free parking time limit should give free park
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotBike);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(0, ticket.getPrice());
    }


    @Test
    @Tag("Free30MinPark")
    @DisplayName("GIVEN a car user staying exactly the number of minutes of the payable parking limit WHEN calculating the fare THEN we get free park")
    public void calculateFareCarWithExactlyFreeLimitMinutesParkingTime() {
        inTime.setTime(outTime.getTime()
                - (Fare.MINUTES_BEFORE_PAYABLE_PARKING_TIME * TimeTestConstants.MINUTES_TO_MILLISECONDS)); //Free park under MINUTES_BEFORE_PAYABLE_PARKING_TIME
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotCar);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(0, ticket.getPrice());
    }


    @Test
    @Tag("Free30MinPark")
    @DisplayName("GIVEN a car user staying 1 minute after the payable parking limit WHEN calculating the fare THEN we get a proportional car fare")
    public void calculateFareCarWithFreeLimitPlusOneMinutesParkingTime() {
        inTime.setTime(outTime.getTime()
                - ((1 + Fare.MINUTES_BEFORE_PAYABLE_PARKING_TIME) * TimeTestConstants.MINUTES_TO_MILLISECONDS));
        //(1 + MINUTES_BEFORE_PAYABLE_PARKING_TIME) minutes parking time should give (1 + MINUTES_BEFORE_PAYABLE_PARKING_TIME) * parking fare per hour
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotCar);

        fareCalculatorService.calculateFare(ticket);

        double expectedDurationInHour = (1 + Fare.MINUTES_BEFORE_PAYABLE_PARKING_TIME) / ConversionConstants.MINUTES_TO_HOUR_DIVIDER;
        double expectedPrice = PriceUtil.getRoundedPrice(expectedDurationInHour * Fare.CAR_RATE_PER_HOUR);
        assertEquals(expectedPrice, ticket.getPrice());
    }

    /* ----------------------------------------------------------------------------------------------------------------------
     *                  5PerCentDiscountForRecurringUser tests
     * ----------------------------------------------------------------------------------------------------------------------
     * GIVEN a correct vehicle type and a recurring user staying 1 hour WHEN calculating the fare
     * THEN we get a 1 hour fare with discount
     *
     * GIVEN a unknown vehicle type and a recurring user staying 1 hour WHEN calculating the fare
     * THEN we get a NullPointerException
     *
     * GIVEN a correct vehicle type and a non recurring user staying 1 hour WHEN calculating the fare
     * THEN we get a 1 hour fare without discount
     *
     * GIVEN a car recurring user staying under the payable parking time limit WHEN calculating the fare
     * THEN park is free
     * -------------------------------------------------------------------------------------------------------------------- */

    @Test
    @Tag("5PerCentDiscountForRecurringUser")
    @DisplayName("GIVEN a correct vehicle type and a recurring user staying 1 hour WHEN calculating the fare \n"
            + " THEN we get a 1 hour fare with discount")
    public void calculateFareForRecurringUser() {
        inTime.setTime(outTime.getTime() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotCar);
        ticket.setWithDiscount(true);

        fareCalculatorService.calculateFare(ticket);

        double expectedPrice = Fare.CAR_RATE_PER_HOUR
                * (1 - (Fare.PERCENTAGE_OF_DISCOUNT_FOR_RECURRING_USER / ConversionConstants.VALUE_TO_PERCENT_DIVIDER));
        expectedPrice = PriceUtil.getRoundedPrice(expectedPrice);
        assertEquals(expectedPrice, ticket.getPrice());
    }


    @Test
    @Tag("5PerCentDiscountForRecurringUser")
    @DisplayName("GIVEN a unknown vehicle type and a recurring user staying 1 hour WHEN calculating the fare \n"
            + " THEN we get a NullPointerException")
    public void calculateFareUnknownTypeForRecurringUser() {
        inTime.setTime(outTime.getTime() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotUnknown);
        ticket.setWithDiscount(true);

        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }


    @Test
    @Tag("5PerCentDiscountForRecurringUser")
    @DisplayName("GIVEN a correct vehicle type and a non recurring user staying 1 hour WHEN calculating the fare\n"
            + " THEN we get a 1 hour fare without discount")
    public void calculateFareForNonRecurringUser() {
        inTime.setTime(outTime.getTime() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotBike);
        ticket.setWithDiscount(false);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }


    @Test
    @Tag("5PerCentDiscountForRecurringUser")
    @DisplayName("GIVEN a car recurring user staying under the payable parking time limit WHEN calculating the fare\n"
            + " THEN park is free")
    public void calculateFareForRecurringUserWithFreeLimitMinutesParkingTime() {
        inTime.setTime(outTime.getTime()
                - (Fare.MINUTES_BEFORE_PAYABLE_PARKING_TIME * TimeTestConstants.MINUTES_TO_MILLISECONDS));
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpotCar);
        ticket.setWithDiscount(true);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(0, ticket.getPrice());
    }
}
