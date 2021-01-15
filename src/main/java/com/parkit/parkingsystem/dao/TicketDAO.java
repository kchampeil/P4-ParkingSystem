package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * save the ticket in the database.
     *
     * @param ticket ticket to save in the database
     * @return true if ticket has been saved, false in case of exception
     */
    public boolean saveTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME, WITH_DISCOUNT)
            //ps.setInt(1,ticket.getId());
            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
            ps.setBoolean(6, ticket.getWithDiscount());
            int updateRowCount = ps.executeUpdate();
            return (updateRowCount == 1);

        } catch (Exception ex) {
            logger.error("Error saving ticket", ex);
            return false;
        } finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
    }

    /**
     * get ticket in the database for a specified vehicle registration number.
     *
     * @param vehicleRegNumber vehicle registration number of which we want to get the ticket
     * @return if found, a Ticket object, else null
     */
    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.GET_TICKET);
            //PARKING_NUMBER, ID, PRICE, IN_TIME, OUT_TIME, TYPE, WITH_DISCOUNT)
            ps.setString(1, vehicleRegNumber);
            rs = ps.executeQuery();
            if (rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
                ticket.setWithDiscount(rs.getBoolean(7));
                return ticket;
            }
        } catch (Exception ex) {
            logger.error("Error fetching next available slot", ex);
        } finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
        return ticket;
    }

    /**
     * get ticket in the database for a specified ticket id.
     *
     * @param ticketId id of the ticket of which we want to get the ticket
     * @return if found, a Ticket object, else null
     */
    public Ticket getTicketOnId(int ticketId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.GET_TICKET_ON_ID);
            //PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME, TYPE, WITH_DISCOUNT)
            ps.setInt(1, ticketId);
            rs = ps.executeQuery();
            if (rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(ticketId);
                ticket.setVehicleRegNumber(rs.getString(2));
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
                ticket.setWithDiscount(rs.getBoolean(7));
            }

        } catch (Exception ex) {
            logger.error("Error fetching next available slot", ex);
        } finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
        return ticket;
    }

    /**
     * update the price and out time of a given ticket (id) in the DB.
     *
     * @param ticket ticket we want to update in the DB
     * @return true if ticket has been updated
     */
    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3, ticket.getId());
            int updateRowCount = ps.executeUpdate();
            return (updateRowCount == 1);
        } catch (Exception ex) {
            logger.error("Error saving ticket info", ex);
        } finally {
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }

    /**
     * get the number of previous paid tickets for a defined vehicleRegNumber.
     * free tickets are excluded.
     *
     * @param vehicleRegNumber vehicle registration number
     * @return the number of previous paid tickets for the vehicle. Returns 0 if the vehicle never been parked with payable tickets.
     */
    public int getNumberOfPreviousParksForVehicle(String vehicleRegNumber) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int nbOfPreviousParksForUser = 0;
        try {
            con = dataBaseConfig.getConnection();
            ps = con.prepareStatement(DBConstants.GET_NB_OF_PREVIOUS_PARKS_FOR_USER);
            //count(ID)
            ps.setString(1, vehicleRegNumber);
            ps.setDouble(2, 0); // free tickets are excluded from the counting
            rs = ps.executeQuery();
            if (rs.next()) {
                nbOfPreviousParksForUser = rs.getInt(1);
            }
        } catch (Exception ex) {
            logger.error("Error searching for previous parks for vehicle", ex);
        } finally {
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            dataBaseConfig.closeConnection(con);

        }
        return nbOfPreviousParksForUser;
    }
}
