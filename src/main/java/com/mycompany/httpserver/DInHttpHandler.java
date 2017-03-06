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

public class DInHttpHandler implements HttpHandler{
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		// TODO Auto-generated method stub
		long start_time = System.currentTimeMillis();
		
		FFHttpServer.logger.debug(String.format("%s -> %s", arg0.getRemoteAddress(), arg0.getRequestURI().toString()));
		// parse request
		Map<String, Object> parameters = new HashMap<String, Object>();
	    String query = arg0.getRequestURI().getRawQuery();
		Utils.parseQuery(query, parameters);
	    
		String response = "";
	    String msg = "";
	    Boolean result = false;
	    byte di_state = 0x00;
	    int code = 0;
	    
	    Object device_id = parameters.get("device_id");
	    Object username = parameters.get("username");
        Object session_id = parameters.get("session_id");
        
		if (username != null && session_id != null && device_id != null) {
			SessionInfo session_info = Utils.user_manager.get(username);
			code = Utils.checkSessionInfo(session_info, (String) session_id);
        	if (code == -1000) {
    			msg = "De nghi dang nhap";
        	} else if (code == -1001) {
    			msg = "Tai khoan bi dang nhap tai mot noi khac, de nghi dang nhap lai";
    		} else if (code == -1002) {
    			msg = "Het phien lam viec, de nghi dang nhap lai";
    		} else if (!session_info.device_lst.containsKey((String)device_id)) {
				code = -4;
    			msg = "Khong co quyen truy cap thiet bi";
    		} else {
    			FFRequest req = new FFRequest(session_info.device_lst.get(device_id), 
    					EnumRequestType.Modbus_0x02, 
    					Unpooled.copiedBuffer(ModbusRTU.genMsgGetDIn((byte) 0x01, 0x00, 0x08)), 
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
    				code = -5;
    				msg = "Timeout";
    			} else if (!req.result) {
					code = -6;
					msg = req.response.toString(Charset.defaultCharset());
				} else if (req.response.readableBytes() < 6) {
					code = -7;
					msg = "Du lieu khong kha dung";
				} else {
					di_state = req.response.getByte(3);					
					msg = "Success";
					result = true;
				}
    		}
		} else {
			code = -1;
			msg = "Request Params Error";
		}
	    
		response = JSONEncoder.genDInResponse(result, di_state, msg, code);	
		FFHttpServer.logger.debug(String.format("%s <- %dms: %s", arg0.getRemoteAddress(), System.currentTimeMillis() - start_time, response));
		
		Utils.sendResponse(arg0, response);
	}
}
