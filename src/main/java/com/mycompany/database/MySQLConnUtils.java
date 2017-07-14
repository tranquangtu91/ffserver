package com.mycompany.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class MySQLConnUtils {
    
    static final Logger logger = Logger.getLogger(MySQLConnUtils.class.getName());

    public static Connection getMySQLConnection() throws ClassNotFoundException, SQLException {
        String hostname = "demomap.ddns.net";
        String username = "root";
        String password = "";
        String dbname = "ffserverdb";
        return getMySQLConnection(hostname, dbname, username, password);
    }

    public static Connection getMySQLConnection(String hostname, String dbname, String username, String password) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            String connURL = String.format("jdbc:mysql://%s:3306/%s?autoReconnect=true&serverTimezone=UTC", hostname, dbname);
            logger.info(connURL);
            conn = DriverManager.getConnection(connURL, username, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.error(String.format("SQLException: %s", ex.getMessage()));
            logger.error("SQLState: " + ex.getSQLState());
            logger.error("VendorError: " + ex.getErrorCode());
        } catch (ClassNotFoundException ex) {
            logger.error(String.format("ClassNotFoundException: %s", ex.getMessage()));
        } catch (InstantiationException ex) {
            logger.error(String.format("Other ex: %s", ex.getMessage()));
        } catch (IllegalAccessException ex) {
            logger.error(String.format("Other ex: %s", ex.getMessage()));
        }
        return conn;
    }
}
