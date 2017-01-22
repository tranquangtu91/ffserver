package com.mycompany.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnUtils {
	
	public static Connection getMySQLConnection() throws ClassNotFoundException, SQLException{
        String hostname = "localhost";
        String username = "root";
        String password = "";
        String dbname = "ffserverdb";
        return getMySQLConnection(hostname, dbname, username, password);
    }
	
	public static Connection getMySQLConnection(String hostname, String dbname, String username, String password) throws ClassNotFoundException, SQLException{
        Class.forName("com.mysql.jdbc.Driver");
        String connURL = "jdbc:mysql://" + hostname + ":3306/" + dbname;
        Connection conn = DriverManager.getConnection(connURL, username, password);
        return conn;
    }
}
