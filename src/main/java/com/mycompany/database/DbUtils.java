package com.mycompany.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
	
	public static void updateOnlineState(String reg_str, Boolean state) throws ClassNotFoundException, SQLException {
		Connection conn = ConnectionUtils.getConnection();
		PreparedStatement pstmt = conn.prepareStatement("Update device_info Set connect_last_time = Now(), online = ? Where regs = ?");
		pstmt.setBoolean(1, state);
		pstmt.setString(2, reg_str);
		pstmt.executeUpdate();
	}
	
	public static void updateAllToOffline() throws ClassNotFoundException, SQLException{
		Connection conn = ConnectionUtils.getConnection();
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("Update device_info Set online = 0");
	}
}
