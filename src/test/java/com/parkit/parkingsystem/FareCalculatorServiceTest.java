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
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    public void calculateFareBike() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    public void calculateFareUnknownType() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * TimeTestConstants.MINUTES_TO_MILLISECONDS)); //45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        double expectedPrice = PriceUtil.getRoundedPrice(0.75 * Fare.BIKE_RATE_PER_HOUR);
        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * TimeTestConstants.MINUTES_TO_MILLISECONDS)); //45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        double expectedPrice = PriceUtil.getRoundedPrice(0.75 * Fare.CAR_RATE_PER_HOUR);
        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 * TimeTestConstants.MINUTES_TO_MILLISECONDS)); //24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

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
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - ((Fare.MINUTES_BEFORE_PAYABLE_PARKING_TIME - 5) * TimeTestConstants.MINUTES_TO_MILLISECONDS));
        //5 minutes under free parking time limit should give free park
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(0, ticket.getPrice());
    }


    @Test
    @Tag("Free30MinPark")
    @DisplayName("GIVEN a car user staying exactly the number of minutes of the payable parking limit WHEN calculating the fare THEN we get free park")
    public void calculateFareCarWithExactlyFreeLimitMinutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (Fare.MINUTES_BEFORE_PAYABLE_PARKING_TIME * TimeTestConstants.MINUTES_TO_MILLISECONDS)); //Free park under MINUTES_BEFORE_PAYABLE_PARKING_TIME
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(0, ticket.getPrice());
    }


    @Test
    @Tag("Free30MinPark")
    @DisplayName("GIVEN a car user staying 1 minute after the payable parking limit WHEN calculating the fare THEN we get a proportional car fare")
    public void calculateFareCarWithFreeLimitPlusOneMinutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - ((1 + Fare.MINUTES_BEFORE_PAYABLE_PARKING_TIME) * TimeTestConstants.MINUTES_TO_MILLISECONDS));
        //(1 + MINUTES_BEFORE_PAYABLE_PARKING_TIME) minutes parking time should give (1 + MINUTES_BEFORE_PAYABLE_PARKING_TIME) * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        double expectedDurationInHour = (1 + Fare.MINUTES_BEFORE_PAYABLE_PARKING_TIME) / ConversionConstants.MINUTES_TO_HOUR_DIVIDER;
        double expectedPrice = PriceUtil.getRoundedPrice(expectedDurationInHour * Fare.CAR_RATE_PER_HOUR);
        assertEquals(expectedPrice, ticket.getPrice());
    }

    /* ----------------------------------------------------------------------------------------------------------------------
     *                  5PerCentDiscountForRecurrentUser tests
     * ----------------------------------------------------------------------------------------------------------------------
     * GIVEN a correct vehicle type and a recurrent user staying 1 hour WHEN calculating the fare
     * THEN we get a 1 hour fare with discount
     *
     * GIVEN a unknown vehicle type and a recurrent user staying 1 hour WHEN calculating the fare
     * THEN we get a NullPointerException
     *
     * GIVEN a correct vehicle type and a non recurrent user staying 1 hour WHEN calculating the fare
     * THEN we get a 1 hour fare without discount
     *
     * GIVEN a car recurrent user staying under the payable parking time limit WHEN calculating the fare
     * THEN park is free
     * -------------------------------------------------------------------------------------------------------------------- */

    @Test
    @Tag("5PerCentDiscountForRecurrentUser")
    @DisplayName("GIVEN a correct vehicle type and a recurrent user staying 1 hour WHEN calculating the fare \n" +
            " THEN we get a 1 hour fare with discount")
    public void calculateFareForRecurrentUser() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setWithDiscount(true);

        fareCalculatorService.calculateFare(ticket);

        double expectedPrice = Fare.CAR_RATE_PER_HOUR * (1 - (Fare.PERCENTAGE_OF_DISCOUNT_FOR_RECURRING_USER / ConversionConstants.VALUE_TO_PERCENT_DIVIDER));
        expectedPrice = PriceUtil.getRoundedPrice(expectedPrice);
        assertEquals(expectedPrice, ticket.getPrice());
    }


    @Test
    @Tag("5PerCentDiscountForRecurrentUser")
    @DisplayName("GIVEN a unknown vehicle type and a recurrent user staying 1 hour WHEN calculating the fare \n" +
            " THEN we get a NullPointerException")
    public void calculateFareUnknownTypeForRecurrentUser() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setWithDiscount(true);

        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }


    @Test
    @Tag("5PerCentDiscountForRecurrentUser")
    @DisplayName("GIVEN a correct vehicle type and a non recurrent user staying 1 hour WHEN calculating the fare\n" +
            " THEN we get a 1 hour fare without discount")
    public void calculateFareForNonRecurrentUser() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setWithDiscount(false);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }


    @Test
    @Tag("5PerCentDiscountForRecurrentUser")
    @DisplayName("GIVEN a car recurrent user staying under the payable parking time limit WHEN calculating the fare\n" +
            " THEN park is free")
    public void calculateFareForRecurrentUserWithFreeLimitMinutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (Fare.MINUTES_BEFORE_PAYABLE_PARKING_TIME * TimeTestConstants.MINUTES_TO_MILLISECONDS));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setWithDiscount(true);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(0, ticket.getPrice());
    }
}
