package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.testconstants.TimeTestConstants;
import com.parkit.parkingsystem.testconstants.VehicleTestConstants;
import com.parkit.parkingsystem.util.DateUtil;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

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
        dateUtil = new DateUtil();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); //CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
        dataBasePrepareService.clearDataBaseEntries();

        Date wantedIncomingTime = new Date();
        wantedIncomingTime.setTime(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS));
        System.out.println("Wanted incoming time: " + wantedIncomingTime);
        when(dateUtil.getCurrentDate()).thenReturn(wantedIncomingTime);
    }

    // TODO-E à voir à la fin si utilisé ou à supprimer
    @AfterAll
    private static void tearDown() {

    }

    @Test
    @DisplayName("Given an incoming vehicle when the incoming process is finished then a ticket is actually saved in DB and the parking table is updated with availability")
    public void testParkingACar() {

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);
        parkingService.processIncomingVehicle();

        //TODO-E check de l'élément mocké ?
        verify(inputReaderUtil, Mockito.times(1)).readSelection();

        //check that a ticket is actually saved in DB (not null when getting ticket in DB)
        Ticket savedTicket = ticketDAO.getTicket(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
        assertNotNull(savedTicket);

        //and Parking table is updated with availability (parkingSpot of the saved ticket is not the next available slot)
        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(savedTicket.getParkingSpot().getParkingType());
        assertNotEquals(savedTicket.getParkingSpot().getId(), nextAvailableSlot);

    }

    @Test
    @DisplayName("Given an exiting vehicle when the exiting process is finished then, in the DB, the ticket has been updated with calculated fare and out time, and parking spot is set to free")
    public void testParkingLotExitForACar() {
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

}
