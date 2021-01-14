package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.DateUtil;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final String VEHICLE_REG_NUMBER_FOR_TESTS = "ABCDEF";

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
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICLE_REG_NUMBER_FOR_TESTS);
        dataBasePrepareService.clearDataBaseEntries();

        Date wantedIncomingTime = new Date();
        wantedIncomingTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        System.out.println("Wanted incoming time: " + wantedIncomingTime);
        when(dateUtil.getCurrentDate()).thenReturn(wantedIncomingTime);
    }

    @AfterAll
    private static void tearDown() {

    }

    @Test
    @DisplayName("Given an incoming vehicle when the incoming process is finished then a ticket is actually saved in DB and the parking table is updated with availability")
    public void testParkingACar() {

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);
        parkingService.processIncomingVehicle();

        //TOASK check de l'élément mocké ?
        verify(inputReaderUtil, Mockito.times(1)).readSelection();

        //check that a ticket is actually saved in DB (not null when getting ticket in DB)
        Ticket savedTicket = ticketDAO.getTicket(VEHICLE_REG_NUMBER_FOR_TESTS);
        assertNotNull(savedTicket);

        //and Parking table is updated with availability (parkingSpot of the saved ticket is not the next available slot)

        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(savedTicket.getParkingSpot().getParkingType());
        assertNotEquals(savedTicket.getParkingSpot().getId(), nextAvailableSlot);

    }

    @Test
    @DisplayName("Given an exiting vehicle when the exiting process is finished then, in the DB, the ticket has been updated with calculated fare and out time, and parking spot is set to free")
    public void testParkingLotExitForACar() {
        // initialize the test with an incoming car 1 hour before current time
        //TOASK pourquoi ne pas faire simplement un incoming plutôt qu'un test complet ? imbrication de tests...
        //testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);
        parkingService.processIncomingVehicle();

        Ticket savedTicket = ticketDAO.getTicket(VEHICLE_REG_NUMBER_FOR_TESTS);
        //TTR
        System.out.println("******* saved ticket id: " + savedTicket.getId() + " ******\n");

        // then exiting
        when(dateUtil.getCurrentDate()).thenReturn(new Date());
        parkingService.processExitingVehicle();

        //TODO: check that the fare generated and out time are populated correctly in the database
        //TODO-M vérifier la requête SQL du getTicket (ne prend qu'un retour mais sans autre critère que le n° d'immat ?
        Ticket updatedTicket = ticketDAO.getTicket(VEHICLE_REG_NUMBER_FOR_TESTS);
        //TTR
        System.out.println("******* updated ticket id: " + updatedTicket.getId() + " ******\n");

        assertNotNull(updatedTicket.getOutTime());
        //TOASK il faut mocker l'objet Date pour gérer la durée entre in et out ?
        assertNotEquals(0.0, updatedTicket.getPrice());

        /*
        System.out.println("updated ticket id: " + updatedTicket.getId());
        System.out.println("updated ticket out time:" + updatedTicket.getOutTime());
        System.out.println("updated ticket price: " + updatedTicket.getPrice());
        */

        // check that parking spot is set to free in DB after exit
        // ie parkingSpot of the updated ticket is the next available slot for this parking type
        int updatedParkingSpotId = updatedTicket.getParkingSpot().getId();
        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(updatedTicket.getParkingSpot().getParkingType());
        assertEquals(updatedParkingSpotId, nextAvailableSlot);

    }

}
