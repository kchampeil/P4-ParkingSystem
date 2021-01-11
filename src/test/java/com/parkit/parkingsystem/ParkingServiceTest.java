package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            //TODO-E à supprimer à la fin
            // existant : besoin de comportement différent selon les tests => à redescendre dans chaque test et adapter
            /*
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
             */

            //TODO-E à supprimer à la fin
            // existant : ne sert que pour la sortie => à mettre dans le test sortie et sortir du BeforeEach
            //when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            //when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() {
        // KC déplacés du Before Each à ici car spécifiques à la sortie
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        // KC fin

        parkingService.processExitingVehicle();

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    // KC
    // TOASK à voir avec Mathieu
    /*@Test
    @Tag("processExiting")
    public void processExitingVehicleTestForUnknownVehicleRegNumber() throws SQLException, ClassNotFoundException {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.getTicket(anyString())).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> parkingService.processExitingVehicle());

        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
    }*/


    @Test
    @Tag("processIncoming")
    @DisplayName("Given a car recurrent user when entering the park then ParkingSpot has been updated, Ticket has been saved and access to get the number of previous parks for user has been done")
    public void processIncomingVehicleTestForCarRecurrentUser() {
        //GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        // KC ajouté
        ticket.setWithDiscount(true);

        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber())).thenReturn(2);

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
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        // KC ajouté
        ticket.setWithDiscount(true);

        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber())).thenReturn(2);

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
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        // KC ajouté
        ticket.setWithDiscount(false);

        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
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
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        // KC ajouté
        ticket.setWithDiscount(false);

        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber())).thenReturn(0);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber());
    }

    //TOASK
 /*   @Test
    @Tag("processIncoming")
    @DisplayName("Given an unknown vehicle type when entering the park then Exception")
    public void processIncomingVehicleTestForUnknownType() {
        //GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setWithDiscount(false);

        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(null)).thenThrow(new SQLException());
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber())).thenReturn(0);

        //WHEN
        assertThrows(SQLException.class, () -> parkingService.processIncomingVehicle());

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber());
    }
*/
    //TODO-H ajouter un test avec un retour accès bdd en exception
    //processIncomingWithSQLException à faire et Display name à faire
    /*@Test
    @Tag("processIncoming")
    @DisplayName("xxxxxx")
    public void processIncomingWithSQLException() {
        //GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        // KC ajouté
        ticket.setWithDiscount(true);

        when(inputReaderUtil.readSelection()).thenReturn(1);
        doThrow(new SQLException()).when(parkingSpotDAO.updateParking(parkingSpot));
        //when(parkingSpotDAO.updateParking(parkingSpot)).thenThrow(new Exception());
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber())).thenReturn(2);

        //WHEN
        assertThrows(SQLException.class, () -> parkingService.processIncomingVehicle());

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNumberOfPreviousParksForVehicle(ticket.getVehicleRegNumber());
    }*/
}
