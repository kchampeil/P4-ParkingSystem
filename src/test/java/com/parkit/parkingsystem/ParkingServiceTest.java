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
import org.junit.jupiter.api.BeforeAll;
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
    private static ParkingSpot parkingSpotCar;
    private static Ticket ticket;
    private static DateUtil dateUtil;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeAll
    private static void setUp() {
        parkingSpotCar = new ParkingSpot(1, ParkingType.CAR, false);
    }

    @BeforeEach
    private void setUpPerTest() {
        dateUtil = new DateUtil();

        ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (TimeTestConstants.ONE_HOUR_IN_MILLISECONDS)));
        ticket.setParkingSpot(parkingSpotCar);
        ticket.setVehicleRegNumber(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

    }

    /* ----------------------------------------------------------------------------------------------------------------------
     *                  processExiting tests
     * ----------------------------------------------------------------------------------------------------------------------
     * GIVEN a parked car WHEN exiting the parking
     * THEN ticket and parking spot are updated
     *
     * GIVEN an unknown vehicle registration number WHEN exiting the park
     * THEN Ticket and ParkingSpot have not been updated
     *
     * GIVEN an error when updating the ticket at the exit, WHEN exiting the park
     * THEN ParkingSpot has not been updated
     * -------------------------------------------------------------------------------------------------------------------- */

    @Test
    @Tag("processExiting")
    @DisplayName("GIVEN a parked car WHEN exiting the parking THEN ticket and parking spot are updated")
    public void processExitingVehicleTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

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


    @Test
    @Tag("processExiting")
    @DisplayName("GIVEN an unknown vehicle registration number, WHEN exiting the park THEN Ticket and ParkingSpot have not been updated")
    public void processExitingVehicleTestWithUnknownVehicleRegNumber() {
        //GIVEN
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
            when(ticketDAO.getTicket(anyString())).thenReturn(null); // ticket not found for this reg number
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
    @DisplayName("GIVEN an error when updating the ticket at the exit, WHEN exiting the park THEN ParkingSpot has not been updated")
    public void processExitingVehicleTestWithErrorOnTicketUpdate() {
        //GIVEN
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

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
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));

    }


    /* ----------------------------------------------------------------------------------------------------------------------
     *                  processIncoming tests
     * ----------------------------------------------------------------------------------------------------------------------
     * GIVEN a recurring user and an available parking spot WHEN entering the park
     * THEN ParkingSpot has been updated, Ticket has been saved
     * and access to get the number of previous parks for user has been done
     *
     * GIVEN a non recurring user and an available parking spot WHEN entering the park
     * THEN ParkingSpot has been updated, Ticket has been saved
     * and access to get the number of previous parks for user has been done
     *
     * GIVEN a known type vehicle and no available Parking Spot WHEN entering the park
     * THEN no ParkingSpot has been updated and no Ticket has been saved
     *
     * GIVEN an unknown vehicle type WHEN entering the park
     * THEN no ParkingSpot has been updated and no Ticket has been saved
     * -------------------------------------------------------------------------------------------------------------------- */

    @Test
    @Tag("processIncoming")
    @DisplayName("GIVEN a recurring user and an available parking spot WHEN entering the park\n"
            + " THEN ParkingSpot has been updated, Ticket has been saved and access to get the number of previous parks for user has been done")
    public void processIncomingVehicleTestForRecurringUser() {
        //GIVEN
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

            when(inputReaderUtil.readSelection()).thenReturn(InteractiveShellTestsConstants.PARKING_TYPE_CAR); //CAR
            when(parkingSpotDAO.getNextAvailableSpot(ParkingType.CAR)).thenReturn(1);
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
            when(ticketDAO.getNumberOfPreviousParksForVehicle(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS)).thenReturn(2);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSpot(any(ParkingType.class));
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNumberOfPreviousParksForVehicle(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
    }


    @Test
    @Tag("processIncoming")
    @DisplayName("GIVEN a non recurring user and an available parking spot WHEN entering the park\n"
            + " THEN ParkingSpot has been updated, Ticket has been saved and access to get the number of previous parks for user has been done")
    public void processIncomingVehicleTestForNonRecurringUser() {
        //GIVEN
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);

            when(inputReaderUtil.readSelection()).thenReturn(InteractiveShellTestsConstants.PARKING_TYPE_BIKE);
            when(parkingSpotDAO.getNextAvailableSpot(ParkingType.BIKE)).thenReturn(1);
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
            when(ticketDAO.getNumberOfPreviousParksForVehicle(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS)).thenReturn(0);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSpot(any(ParkingType.class));
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNumberOfPreviousParksForVehicle(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
    }


    @Test
    @Tag("processIncoming")
    @DisplayName("GIVEN a known type vehicle and no available Parking Spot WHEN entering the park\n"
            + " THEN no ParkingSpot has been updated and no Ticket has been saved")
    public void processIncomingVehicleTestWithUnavailableParkingSpot() {
        //GIVEN
        try {
            when(inputReaderUtil.readSelection()).thenReturn(InteractiveShellTestsConstants.PARKING_TYPE_CAR);
            when(parkingSpotDAO.getNextAvailableSpot(ParkingType.CAR)).thenReturn(-1);
            //no available spot => return -1

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSpot(any(ParkingType.class));
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(0)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(0)).getNumberOfPreviousParksForVehicle(VehicleTestConstants.VEHICLE_REG_NUMBER_FOR_TESTS);
    }


    @Test
    @Tag("processIncoming")
    @DisplayName("GIVEN an unknown vehicle type WHEN entering the park THEN no ParkingSpot has been updated and no Ticket has been saved")
    public void processIncomingVehicleTestForUnknownType() {

        //GIVEN
        try {
            when(inputReaderUtil.readSelection()).thenReturn(InteractiveShellTestsConstants.PARKING_TYPE_UNKNOWN);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, dateUtil);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(0)).getNumberOfPreviousParksForVehicle(anyString());
        verify(ticketDAO, Mockito.times(0)).saveTicket(any(Ticket.class));

    }

}
