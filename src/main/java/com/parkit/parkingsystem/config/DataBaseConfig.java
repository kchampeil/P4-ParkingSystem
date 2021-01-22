package com.parkit.parkingsystem.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.apache.logging.log4j.core.util.Loader.getClassLoader;

/**
 * Database configuration.
 */

public class DataBaseConfig {

    private static final Logger logger = LogManager.getLogger("DataBaseConfig");

    /**
     * open a connection to the DB, based on the DataBaseConfig.properties.
     *
     * @return a connection
     * @throws ClassNotFoundException if jdbc.driver.class is not found
     * @throws SQLException if exception while executing SQL instructions
     * @throws IOException if exception while reading the DataBaseConfig.properties file
     */
    public Connection getConnection() throws ClassNotFoundException, SQLException, IOException {
        logger.info("Create DB connection");
        Properties properties = new Properties();
        try (InputStream inputStream = getClassLoader().getResourceAsStream("DataBaseConfig.properties")) {
            properties.load(inputStream);
        }
        Class.forName(properties.getProperty("jdbc.driver.class"));

        String url = properties.getProperty("jdbc.url");
        String login = properties.getProperty("jdbc.login");
        String password = properties.getProperty("jdbc.password");
        return DriverManager.getConnection(url, login, password);
    }

    /**
     * close a given connection to the DB.
     *
     * @param con the connection to close
     */
    public void closeConnection(final Connection con) {
        if (con != null) {
            try {
                con.close();
                logger.info("Closing DB connection");
            } catch (SQLException e) {
                logger.error("Error while closing connection", e);
            }
        }
    }

    /**
     * close a given prepared statement.
     *
     * @param ps prepared statement to close
     */
    public void closePreparedStatement(final PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
                logger.info("Closing Prepared Statement");
            } catch (SQLException e) {
                logger.error("Error while closing prepared statement", e);
            }
        }
    }

    /**
     * close a given result set.
     *
     * @param rs result set to close
     */
    public void closeResultSet(final ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                logger.info("Closing Result Set");
            } catch (SQLException e) {
                logger.error("Error while closing result set", e);
            }
        }
    }
}
