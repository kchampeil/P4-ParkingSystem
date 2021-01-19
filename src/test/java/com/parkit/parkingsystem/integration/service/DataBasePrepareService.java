package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

import java.sql.Connection;

public class DataBasePrepareService {

    private final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    /**
     * prepare the DB by clearing all previous entries of the TICKET table
     * and resetting availability of parking spot in the PARKING table.
     */
    public void clearDataBaseEntries() {

        try (Connection connection = dataBaseTestConfig.getConnection()) {

            //set parking entries to available
            connection.prepareStatement("update parking set available = true").execute();

            //clear ticket entries;
            connection.prepareStatement("truncate table ticket").execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
