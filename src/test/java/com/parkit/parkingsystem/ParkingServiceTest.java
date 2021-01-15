package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.testconstants.InteractiveShellTestsConstants;
import com.parkit.parkingsystem.testconstants.TimeTestConstants;
import com.parkit.parkingsystem.testconstants.VehicleTestConstants;
import com.parkit.parkingsystem.util.DateUtil;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;
    private static DateUtil dateUtil;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    //TODO-M voir mutualisation des autres inits
    @BeforeEach
    private void setUpPerTest() {
        dateUtil = new DateUtil();
    }
/*    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

 */

    // ---------------------------------------------------------------
    //                   processExiting tests
    // ---------------------------------------------------------------

    @Test
    @Tag("processExiting")
    public void processExitingVehicleTest() {
        try {
            //TODO-E mettre constante
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }

        parkingService.processExitingVehicle();

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));

    }

    //TODO-H à revoir si test du dernier ticket ou pas
    @Test
    @Tag("processExiting")
    @DisplayName("Given an unknown vehicle regnumber, when exiting the park then Ticket and ParkingSpot have not been updated")
    public void processExitingVehicleTestWithUnknownVehicleRegNumber() {
        //GIVEN
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
            when(ticketDAO.getTicket(anyString())).thenReturn(null); // ticket not found
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(0)).updateTicket(any(Ticket.class));

    }

    @Test
    @Tag("processExiting")
    @DisplayName("Given an error when updating the ticket at the exit, when exiting the park then ParkingSpot has not been updated")
    public void processExitingVehicleTestWithErrorOnTicketUpdate() {
        //GIVEN
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false); // ticket update error

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));

    }

    @Test
    @Tag("processExiting")
    @DisplayName("Given an unknown vehicle RegNumber when exiting the park then no Ticket and no ParkingSpot have been updated")
    public void processExitingVehicleTestForUnknownVehicleRegNumber() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

            when(ticketDAO.getTicket(anyString())).thenReturn(null);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }

        parkingService.processExitingVehicle();

        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(ticketDAO, Mockito.times(0)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));

    }

    // ---------------------------------------------------------------
    //                   processIncoming tests
    // ---------------------------------------------------------------
    @Test
    @Tag("processIncoming")
    @DisplayName("Given a car recurrent user when entering the park then ParkingSpot has been updated, Ticket has been saved and access to get the number of previous parks for user has been done")
    public void processIncomingVehicleTestForCarRecurrentUser() {
        //GIVEN
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

            when(inputReaderUtil.readSelection()).thenReturn(InteractiveShellTestsConstants.PARKING_TYPE_CAR); //CAR
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
            when(ticketDAO.getNumberOfPreviousParksForVehicle(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS)).thenReturn(2);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
        ticket.setWithDiscount(true);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber());
    }

    @Test
    @Tag("processIncoming")
    @DisplayName("Given a bike recurrent user when entering the park then ParkingSpot has been updated, Ticket has been saved and access to get the number of previous parks for user has been done")
    public void processIncomingVehicleTestForBikeRecurrentUser() {
        //GIVEN
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

            when(inputReaderUtil.readSelection()).thenReturn(InteractiveShellTestsConstants.PARKING_TYPE_BIKE);
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(1);
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
            when(ticketDAO.getNumberOfPreviousParksForVehicle(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS)).thenReturn(2);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
        ticket.setWithDiscount(true);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber());
    }

    @Test
    @Tag("processIncoming")
    @DisplayName("Given a car non recurrent user when entering the park then ParkingSpot has been updated, Ticket has been saved and access to get the number of previous parks for user has been done")
    public void processIncomingVehicleTestForCarNonRecurrentUser() {
        //GIVEN
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

            when(inputReaderUtil.readSelection()).thenReturn(InteractiveShellTestsConstants.PARKING_TYPE_CAR);
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
        ticket.setWithDiscount(false);
        when(ticketDAO.getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber())).thenReturn(0);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber());
    }

    @Test
    @Tag("processIncoming")
    @DisplayName("Given a bike non recurrent user when entering the park then ParkingSpot has been updated, Ticket has been saved and access to get the number of previous parks for user has been done")
    public void processIncomingVehicleTestForBikeNonRecurrentUser() {
        //GIVEN
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

            when(inputReaderUtil.readSelection()).thenReturn(InteractiveShellTestsConstants.PARKING_TYPE_BIKE);
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(1);
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
            when(ticketDAO.getNumberOfPreviousParksForVehicle(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS)).thenReturn(0);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
        ticket.setWithDiscount(true);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber());
    }

    @Test
    @Tag("processIncoming")
    @DisplayName("Given no available Parking Spot when entering the park then no ParkingSpot has been updated and no Ticket has been saved")
    public void processIncomingVehicleTestWithUnavailableParkingSpot() {
        //GIVEN
        try {
            when(inputReaderUtil.readSelection()).thenReturn(InteractiveShellTestsConstants.PARKING_TYPE_CAR);
            //no available spot => return -1
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
        ticket.setWithDiscount(false);

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(0)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(0)).getNumberOfPreviousParksForVehicle(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
    }

    @Test
    @Tag("processIncoming")
    @DisplayName("Given an unknown vehicle type when entering the park then no ParkingSpot has been updated and no Ticket has been saved")
    public void processIncomingVehicleTestForUnknownType() {

        //GIVEN
        try {
            when(inputReaderUtil.readSelection()).thenReturn(InteractiveShellTestsConstants.PARKING_TYPE_UNKNOWN);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
        ticket.setWithDiscount(false);

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(0)).getNumberOfPreviousParksForVehicle(anyString());
        verify(ticketDAO, Mockito.times(0)).saveTicket(any(Ticket.class));

    }

}