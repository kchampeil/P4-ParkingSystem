package com.parkit.parkingsystem.constants;

public class DBConstants {

    public static final String GET_NEXT_PARKING_SPOT = "select min(PARKING_NUMBER) from parking where AVAILABLE = true and TYPE = ?";
    public static final String UPDATE_PARKING_SPOT = "update parking set available = ? where PARKING_NUMBER = ?";

    public static final String SAVE_TICKET = "insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME, WITH_DISCOUNT) values(?,?,?,?,?,?)";
    public static final String UPDATE_TICKET = "update ticket set PRICE=?, OUT_TIME=? where ID=?";
    public static final String GET_TICKET = "select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE, t.WITH_DISCOUNT from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=? and OUT_TIME is null order by t.IN_TIME  limit 1";
    public static final String GET_TICKET_ON_ID = "select t.PARKING_NUMBER, t.VEHICLE_REG_NUMBER, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE, t.WITH_DISCOUNT from ticket t,parking p where p.parking_number = t.parking_number and t.ID=?";

    public static final String GET_NB_OF_PREVIOUS_PARKS_FOR_USER = "select count(ID) from ticket where VEHICLE_REG_NUMBER=? and PRICE<>?";

}
