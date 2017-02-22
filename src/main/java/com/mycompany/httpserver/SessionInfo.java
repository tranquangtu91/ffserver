package com.mycompany.httpserver;

import java.net.InetSocketAddress;

public class SessionInfo {
	public String session_id;
	public InetSocketAddress remote_addr;
	public enum EnumPermission {
		Admintrator, 
		Guest
	}
	public EnumPermission permission;
}
