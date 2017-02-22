package com.mycompany.database;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbUtils {
	public static byte[] generateMD5Arr(String content) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] bytesOfMessage = content.getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest = md.digest(bytesOfMessage);
		return thedigest;
	}
	
	public static void createUser(String username, String password, int permission) throws SQLException, ClassNotFoundException, NoSuchAlgorithmException, UnsupportedEncodingException {
		Connection conn = ConnectionUtils.getConnection();
		PreparedStatement pstmt = conn.prepareStatement("Insert user_info (username, password, permission) Values (?, ?, ?)");
		pstmt.setString(1, username);
		pstmt.setBytes(2, generateMD5Arr(username + password + permission));
		pstmt.setInt(3, permission);
		pstmt.executeUpdate();
	}
	
	public static ResultSet getUser(String username) throws ClassNotFoundException, SQLException {
		Connection conn = ConnectionUtils.getConnection();
		PreparedStatement pstmt = conn.prepareStatement("Select * From user_info Where username = ?");
		pstmt.setString(1, username);
		ResultSet rs = pstmt.executeQuery();
		return rs;
	}
	
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
	
	public static void createDevice(String name, String reg_str, String desc, Double lat, Double lng) throws ClassNotFoundException, SQLException {
		Connection conn = ConnectionUtils.getConnection();
		PreparedStatement pstmt = conn.prepareStatement("Insert device_info (name, regs, description, latitude, longitude, online) Values (?, ?, ?, ?, ?, 0)");
		pstmt.setString(1, name);
		pstmt.setString(2, reg_str);
		pstmt.setString(3, desc);
		pstmt.setDouble(4, lat);
		pstmt.setDouble(5, lng);
		pstmt.executeUpdate();
	}
	
	public static void RemoveDevice(String reg_str) throws SQLException, ClassNotFoundException {
		Connection conn = ConnectionUtils.getConnection();
		PreparedStatement pstmt = conn.prepareStatement("Delete From device_info Where regs = ?");
		pstmt.setString(1, reg_str);
		pstmt.executeUpdate();
	}
	
	public static void updateDevice(String name, String reg_str, String desc, Double lat, Double lng) throws ClassNotFoundException, SQLException {
		Connection conn = ConnectionUtils.getConnection();
		PreparedStatement pstmt = conn.prepareStatement("Update device_info Set regs = ?, description = ?, latitude = ?, longitude = ? Where name = ?");
		pstmt.setString(1, reg_str);
		pstmt.setString(2, desc);
		pstmt.setDouble(3, lat);
		pstmt.setDouble(4, lng);
		pstmt.setString(5, name);
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
