package com.mycompany.database;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionUtils {
	public static Connection conn = null;
	
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
    	if (conn == null || conn.isClosed()) {
    		conn = MySQLConnUtils.getMySQLConnection();
    	}
    	return conn;
    }
   
    public static void closeQuietly(Connection conn) {
    	try {
    		conn.close();
    	} catch (Exception e) {
    	}
    }

    public static void rollbackQuietly(Connection conn) {
    	try {
    		conn.rollback();
    	} catch (Exception e) {
    	}
    }
}