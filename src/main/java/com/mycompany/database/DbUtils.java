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
	
	public static ResultSet getDeviceInfo(String name) throws ClassNotFoundException, SQLException {
		Connection conn = ConnectionUtils.getConnection();
		PreparedStatement pstmt = conn.prepareStatement("Select * From device_info Where name = ?");
		pstmt.setString(1, name);
		ResultSet rs = pstmt.executeQuery();
		return rs;
	}
	
	public static void createDevice(String name, String reg_str, String desc) throws ClassNotFoundException, SQLException {
		Connection conn = ConnectionUtils.getConnection();
		PreparedStatement pstmt = conn.prepareStatement("Insert device_info (name, regs, description, online) Values (?, ?, ?, 0)");
		pstmt.setString(1, name);
		pstmt.setString(2, reg_str);
		pstmt.setString(3, desc);
		pstmt.executeUpdate();
	}
	
	public static void updateDevice(String name, String reg_str, String desc) throws ClassNotFoundException, SQLException {
		Connection conn = ConnectionUtils.getConnection();
		PreparedStatement pstmt = conn.prepareStatement("Update device_info Set regs = ?, description = ? Where name = ?");
		pstmt.setString(1, reg_str);
		pstmt.setString(2, desc);
		pstmt.setString(3, name);
		pstmt.executeUpdate();
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
