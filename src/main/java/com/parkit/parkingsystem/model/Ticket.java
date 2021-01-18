package com.parkit.parkingsystem.model;

import java.util.Date;

public class Ticket {
    private int id;
    private ParkingSpot parkingSpot;
    private String vehicleRegNumber;
    private double price;
    private Date inTime;
    private Date outTime;
    private boolean withDiscount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getInTime() {
        return inTime != null ? new Date(inTime.getTime()) : null;
    }

    public void setInTime(Date inTime) {
        this.inTime = inTime != null ? new Date(inTime.getTime()) : null;
    }

    public Date getOutTime() {
        return outTime != null ? new Date(outTime.getTime()) : null;
    }

    public void setOutTime(Date outTime) {
        this.outTime = outTime != null ? new Date(outTime.getTime()) : null;
    }

    public void setWithDiscount(boolean withDiscount) {
        this.withDiscount = withDiscount;
    }

    public boolean getWithDiscount() {
        return withDiscount;
    }
}
