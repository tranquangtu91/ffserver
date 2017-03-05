package com.mycompany.httpserver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.ffserver.FFRequest;
import com.mycompany.ffserver.FFRequest.EnumRequestType;
import com.mycompany.main.MainApplication;
import com.mycompany.modbus.ModbusRTU;
import com.mycompany.utils.JSONEncoder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.netty.buffer.Unpooled;

public class DOutHttpHandler implements HttpHandler{
	
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		long start_time = System.currentTimeMillis();
		
		FFHttpServer.logger.debug(String.format("%s -> %s", arg0.getRemoteAddress(), arg0.getRequestURI().toString()));
		// parse request
		Map<String, Object> parameters = new HashMap<String, Object>();
	    String query = arg0.getRequestURI().getRawQuery();
		Utils.parseQuery(query, parameters);
	    
	    String response = "";
	    String msg = "";
	    Boolean result = false;
	    int code = 0;
	    
	    Object device_id = parameters.get("device_id");
	    Object username = parameters.get("username");
        Object session_id = parameters.get("session_id");
	    Object dout = parameters.get("dout");
	    Object state = parameters.get("state");
		if (username != null && session_id != null && device_id != null && dout != null && state != null) {
			SessionInfo session_info = FFHttpServer.user_manager.get(username);
        	if (session_info == null || !session_info.remote_addr.equals(arg0.getRemoteAddress().getAddress())) {
    			code = -2;
    			msg = "De nghi dang nhap";
        	} else if (!session_info.session_id.equals((String)session_id)) {
        		code = -3;
    			msg = "Tai khoan bi dang nhap tai mot noi khac, de nghi dang nhap lai";
    		} else if (session_info.expiry_time < System.currentTimeMillis()) {
    			code = -4;
    			msg = "Het phien lam viec, de nghi dang nhap lai";
    		} else if (!session_info.device_lst.containsKey((String)device_id)) {
				code = -5;
    			msg = "Khong co quyen truy cap thiet bi";
    		} else {
				FFRequest req = new FFRequest(session_info.device_lst.get(device_id), 
						EnumRequestType.Modbus_0x05, 
						Unpooled.copiedBuffer(ModbusRTU.GenMsg_SetDo((byte) 0x01, Integer.parseInt((String) dout), Boolean.parseBoolean((String)state))), 
						5000);
				MainApplication.ff_server.addReqToQueue(req);
				
				while (!req.have_response && 
						(req.request_create_time + req.request_time_out_ms + 5000) > System.currentTimeMillis()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}     
				
    			if (!req.have_response) {
    				code = -6;
    				msg = "Timeout";
    			} else if (!req.result) {
					code = -7;
					msg = req.response.toString(Charset.defaultCharset());
				} else if (!req.response.equals(req.request)) {
					code = -8;
					msg = "Khong dieu khien duoc";
				} else {				
					msg = "Success";
					result = true;
				}
    		}
		} else {
			msg = "Request Params Error";
		}
	    
		response = JSONEncoder.genGenericResponse(result, msg, code);
		FFHttpServer.logger.debug(String.format("%s <- %dms: %s", arg0.getRemoteAddress(), System.currentTimeMillis() - start_time, response));
		
		Utils.sendResponse(arg0, response);
	}
}
