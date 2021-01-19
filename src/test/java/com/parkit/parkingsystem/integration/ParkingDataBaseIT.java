package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ConversionConstants;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.testconstants.InteractiveShellTestsConstants;
import com.parkit.parkingsystem.testconstants.TimeTestConstants;
import com.parkit.parkingsystem.testconstants.VehicleTestConstants;
import com.parkit.parkingsystem.util.DateUtil;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    private Date wantedIncomingTime = new Date();

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private static DateUtil dateUtil;

    @BeforeAll
    private static void setUp() throws Exception {

        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();

    }

    @BeforeEach
    private void setUpPerTest() throws Exception {

        when(inputReaderUtil.readSelection()).thenReturn(InteractiveShellTestsConstants.PARKING_TYPE_CAR); //CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

        wantedIncomingTime.setTime(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        when(dateUtil.getCurrentDate()).thenReturn(wantedIncomingTime);

        dataBasePrepareService.clearDataBaseEntries();

    }

    @AfterAll
    private static void tearDown() {
        // clean test database after all tests
        dataBasePrepareService.clearDataBaseEntries();
    }


    @Test
    @DisplayName("GIVEN an incoming vehicle WHEN the incoming process is finished\n" +
            " THEN a ticket is actually saved in DB and the parking table is updated with availability")
    public void testParkingACar() {

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);
        parkingService.processIncomingVehicle();

        verify(inputReaderUtil, Mockito.times(1)).readSelection();

        //check that a ticket is actually saved in DB (not null when getting ticket in DB)
        Ticket savedTicket = ticketDAO.getTicket(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
        assertNotNull(savedTicket);

        //and Parking table is updated with availability (parkingSpot of the saved ticket is not the next available slot)
        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(savedTicket.getParkingSpot().getParkingType());
        assertNotEquals(savedTicket.getParkingSpot().getId(), nextAvailableSlot);

    }

    @Test
    @DisplayName("GIVEN an exiting vehicle WHEN the exiting process is finished\n" +
            " THEN, in the DB, the ticket has been updated with calculated fare and out time, and parking spot is set to free")
    public void testParkingLotExit() {
        // initialize the test with an incoming car 1 hour before current time
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);
        parkingService.processIncomingVehicle();
        Ticket savedTicket = ticketDAO.getTicket(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

        // then exiting
        Date expectedOutTime = new Date();
        when(dateUtil.getCurrentDate()).thenReturn(expectedOutTime);
        parkingService.processExitingVehicle();

        //check that the fare generated and out time are populated correctly in the database
        double expectedPrice = Fare.CAR_RATE_PER_HOUR;
        Ticket updatedTicket = ticketDAO.getTicketOnId(savedTicket.getId());

        assertEquals(expectedPrice, updatedTicket.getPrice());

        assertTrue(
                Math.abs(expectedOutTime.getTime() - updatedTicket.getOutTime().getTime())
                        < TimeTestConstants.ONE_SECOND_IN_MILLISECONDS);

        // check that parking spot is set to free in DB after exit
        // ie parkingSpot of the updated ticket is the next available slot for this parking type
        int updatedParkingSpotId = updatedTicket.getParkingSpot().getId();
        assertTrue(parkingSpotDAO.getParkingSpotAvailability(updatedParkingSpotId));

    }


    @Test
    //TODO-E revoir le display name
    @DisplayName("GIVEN an exiting vehicle of a recurrent user WHEN the last exiting process is finished\n" +
            " THEN, in the DB, the ticket has been updated with calculated fare and out time, and parking spot is set to free")
    public void testParkingLotExitForARecurrentUser() {

        System.out.println("***** Initialization with the first park *****");

        // initialize the test with an incoming car 3 hours before current time
        wantedIncomingTime.setTime(System.currentTimeMillis() - 3 * (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        when(dateUtil.getCurrentDate()).thenReturn(wantedIncomingTime);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);
        parkingService.processIncomingVehicle();
        Ticket firstSavedTicket = ticketDAO.getTicket(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

        // then exiting 1 hour later (ie 2 hours before current time)
        Date wantedExitingTime = new Date();
        wantedExitingTime.setTime(System.currentTimeMillis() - 2 * (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        when(dateUtil.getCurrentDate()).thenReturn(wantedExitingTime);
        parkingService.processExitingVehicle();
        firstSavedTicket = ticketDAO.getTicketOnId(firstSavedTicket.getId());

        System.out.println("***** Second park for test *****");

        // the second incoming is 1 hour before current time
        wantedIncomingTime.setTime(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        when(dateUtil.getCurrentDate()).thenReturn(wantedIncomingTime);
        parkingService.processIncomingVehicle();
        Ticket secondSavedTicket = ticketDAO.getTicket(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
        assertTrue(secondSavedTicket.getWithDiscount());

        // then exiting for the second time
        wantedExitingTime.setTime(System.currentTimeMillis());
        when(dateUtil.getCurrentDate()).thenReturn(wantedExitingTime);
        parkingService.processExitingVehicle();

        //check that second saved ticket is different from the first
        assertNotEquals(firstSavedTicket.getId(),secondSavedTicket.getId());

        //check that the discount is applied to fare generated and saved in the database
        double expectedPrice = Fare.CAR_RATE_PER_HOUR
                *(1-Fare.PERCENTAGE_OF_DISCOUNT_FOR_RECURRING_USER/ ConversionConstants.VALUE_TO_PERCENT_DIVIDER);
        secondSavedTicket = ticketDAO.getTicketOnId(secondSavedTicket.getId());

        assertEquals(expectedPrice, secondSavedTicket.getPrice());

    }

}
