package com.parkit.parkingsystem;

import com.parkit.parkingsystem.service.InteractiveShell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Application for managing the parking system.
 */
public class App {
    private static final Logger logger = LogManager.getLogger("App");

    /**
     * main method of our application.
     *
     * @param args xxx
     */
    public static void main(String[] args) {
        logger.info("Initializing Parking System");
        InteractiveShell.loadInterface();
    }
}
