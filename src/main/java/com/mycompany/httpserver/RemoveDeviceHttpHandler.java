package com.mycompany.httpserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.database.DbUtils;
import com.mycompany.main.MainApplication;
import com.mycompany.utils.JSONEncoder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RemoveDeviceHttpHandler implements HttpHandler{
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
        int code = 0;

        Object device_id = parameters.get("device_id");
        Object username = parameters.get("username");
        Object session_id = parameters.get("session_id");
        
    	if (username != null && session_id != null && device_id != null) {
    		SessionInfo session_info = FFHttpServer.user_manager.get(username);
    		int id = Integer.parseInt((String)device_id);
        	if (session_info == null || !session_info.session_id.equals((String)session_id) || !session_info.remote_addr.equals(arg0.getRemoteAddress().getAddress())) {
    			code = -2;
    			msg = "De nghi dang nhap";
    		} else if (session_info.expiry_time < System.currentTimeMillis()) {
    			code = -3;
    			msg = "Het phien lam viec, de nghi dang nhap lai";
    		} else if (!session_info.device_lst.containsKey((String)device_id)) {
				code = -4;
    			msg = "Khong co quyen xoa thiet bi";
			} else {
				String reg_str = (String) session_info.device_lst.get(device_id);
				try {
					DbUtils.removeDevice(session_info.user_id, id);
					result = true;
					msg = "Success";

					MainApplication.ff_server.removeRegDevice(reg_str);
					session_info.device_lst.remove(device_id);
				} catch (Exception e) {
					code = -5;
					msg = e.getMessage();
				}
			}
    	} else {
    		code = -1;
    		msg = "Request Params Error";
    	}
    	
    	response = JSONEncoder.genGenericResponse(result, msg, code);	
		FFHttpServer.logger.debug(String.format("%s <- %dms: %s", arg0.getRemoteAddress(), System.currentTimeMillis() - start_time, response));

        Utils.sendResponse(arg0, response);
	}
}
