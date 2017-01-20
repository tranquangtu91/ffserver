package com.mycompany.ffserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class FFRequest {
	public String reg_str;
	public EnumRequestType request_type;
	
	public enum EnumRequestType {Modbus_0x02, Modbus_0x04, Modbus_0x05};
	
	public int request_time_out_ms;
	public long request_create_time;
	public ByteBuf request;
	public ByteBuf response;
	public Boolean have_response;
	public Boolean is_waiting;
	public Boolean result;
	
	public FFRequest(String reg_str, EnumRequestType req_type, ByteBuf req, int reg_time_out_ms) {
		this.reg_str = reg_str;
		this.request_type = req_type;
		this.request = req;
		this.request_time_out_ms = reg_time_out_ms;
		this.request_create_time = System.currentTimeMillis();
		this.response = Unpooled.buffer(2048);
		this.have_response = false;
		this.is_waiting = false;
		this.result = false;
	}
}
