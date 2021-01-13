package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final String VEHICLE_REG_NUMBER_FOR_TESTS = "ABCDEF";

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

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
        when(inputReaderUtil.readSelection()).thenReturn(1); //CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICLE_REG_NUMBER_FOR_TESTS);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {

    }

    @Test
    public void testParkingACar() {

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
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
    public void testParkingLotExit() {
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
        //TODO-H : check that parking spot is set to free after exit (parking table is updated with availability)
    }

}
