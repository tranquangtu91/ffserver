package com.mycompany.httpserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.database.DbUtils;
import com.mycompany.main.MainApplication;
import com.mycompany.utils.JSONEncoder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CreateDeviceHttpHandler implements HttpHandler{
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		// TODO Auto-generated method stub
		long start_time = System.currentTimeMillis();
		
		FFHttpServer.logger.debug(String.format("%s -> %s", arg0.getRemoteAddress(), arg0.getRequestURI().toString()));
		// parse request
		Map<String, Object> body_parameters = new HashMap<String, Object>();
		Map<String, Object> uri_parameters = new HashMap<String, Object>();
		int contentLength = Integer.parseInt(arg0.getRequestHeaders().getFirst("Content-length"));
		byte[] data = new byte[contentLength];
		arg0.getRequestBody().read(data);
		String query = new String(data);
        Utils.parseQuery(query, body_parameters);
        query = arg0.getRequestURI().getRawQuery();
        Utils.parseQuery(query, uri_parameters);
        
        String response = "";
        String msg = "";
        Boolean result = false;
        int code = 0;
        
        Object username = uri_parameters.get("username");
        Object session_id = uri_parameters.get("session_id");
        Object reg_str = body_parameters.get("reg_str");
        Object name = body_parameters.get("name");
        Object desc = body_parameters.get("desc");
        Object lat = body_parameters.get("lat");
        Object lng = body_parameters.get("lng");
        if (username != null && session_id != null && reg_str != null && name != null) {
        	SessionInfo session_info = FFHttpServer.user_manager.get(username);
        	if (session_info == null || !session_info.session_id.equals((String)session_id) || !session_info.remote_addr.equals(arg0.getRemoteAddress().getAddress())) {
    			code = -2;
    			msg = "De nghi dang nhap";
    		} else if (session_info.expiry_time < System.currentTimeMillis()) {
    			code = -3;
    			msg = "Het phien lam viec, de nghi dang nhap lai";
    		} else {
		    	try {
					Integer device_id = DbUtils.createDevice(session_info.user_id, (String)name, (String) reg_str, 
									(String) desc, lat == null ? 0: Double.parseDouble((String)lat), 
									lng == null ? 0 : Double.parseDouble((String)lng));
					msg = "Success";
					result = true;

					MainApplication.ff_server.addRegDevice((String) reg_str);
					session_info.device_lst.put(device_id.toString(), (String) reg_str);
				} catch (Exception e) {
					code = -4;
					msg = e.getMessage();
				}
    		}
        } else {
        	code = -1;
        	msg = "Request Params Error";
        }
    	
    	response = JSONEncoder.genGenericDeviceResponse(result, msg, code);
		FFHttpServer.logger.debug(String.format("%s <- %dms: %s", arg0.getRemoteAddress(), System.currentTimeMillis() - start_time, response));

        Utils.sendResponse(arg0, response);
	}
}
