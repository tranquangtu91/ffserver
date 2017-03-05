package com.mycompany.httpserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.mycompany.utils.JSONEncoder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class LogoutHttpHandler implements HttpHandler{
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
	    
	    Object username = parameters.get("username");
	    Object session_id = parameters.get("session_id");
	    
	    if (username != null && session_id != null) {
			SessionInfo session_info = FFHttpServer.user_manager.get(username);
			if (session_info == null || !session_info.session_id.equals((String)session_id) || !session_info.remote_addr.equals(arg0.getRemoteAddress().getAddress())) {
				code = -2;
				msg = "De nghi dang nhap";
			} else if (session_info.expiry_time < System.currentTimeMillis()) {
				code = -3;
				msg = "Het phien lam viec, de nghi dang nhap lai";
			} else {
				FFHttpServer.user_manager.remove(username);
				result = true;
				msg = "Success";
			}
	    } else {
	    	msg = "Request Params Error";
	    	code = -1;
	    }
		
		response = JSONEncoder.genGenericResponse(result, msg, code);
		FFHttpServer.logger.debug(String.format("%s <- %dms: %s", arg0.getRemoteAddress(), System.currentTimeMillis() - start_time, response));

	    Utils.sendResponse(arg0, response);
	}
	
}
