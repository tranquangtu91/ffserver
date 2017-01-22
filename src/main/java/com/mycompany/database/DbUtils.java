package com.mycompany.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbUtils {
	public static ResultSet getDeviceList() throws ClassNotFoundException, SQLException {
		Connection conn = ConnectionUtils.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("Select * From device_info");
		return rs;
	}
}
