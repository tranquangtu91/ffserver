package com.mycompany.httpserver;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.database.DbUtils;

public class SessionInfo {
	public int user_id;
	public String username;
	public String session_id;
	public InetAddress remote_addr;
	public enum EnumPermission {
		Unkown,
		Admintrator, 
		Guest
	}
	public EnumPermission permission;
	public long expiry_time;
	Map<String, String> device_lst;	//<device_id, device_regs>
	
	public SessionInfo() {
		// TODO Auto-generated constructor stub
		extendTime();
		device_lst = new HashMap<String, String>();
	}
	
	public void extendTime() {
		expiry_time = System.currentTimeMillis() + 3600000;
	}
	
	public void update_device_list() throws ClassNotFoundException, SQLException {
		ResultSet rs;
		device_lst.clear();
		if (permission == EnumPermission.Admintrator) {
			rs = DbUtils.getDeviceList();
		}
		else {
			rs = DbUtils.getDeviceList(user_id);
		}
		while (rs.next()) {
			device_lst.put(rs.getString("id"), rs.getString("regs"));
		}
	}
}
