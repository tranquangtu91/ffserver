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
			SessionInfo session_info = Utils.user_manager.get(username);
			code = Utils.checkSessionInfo(session_info, (String) session_id);
        	if (code == -1000) {
    			msg = "De nghi dang nhap";
        	} else if (code == -1001) {
    			msg = "Tai khoan bi dang nhap tai mot noi khac, de nghi dang nhap lai";
    		} else if (code == -1002) {
    			msg = "Het phien lam viec, de nghi dang nhap lai";
    		} else {
				Utils.user_manager.remove(username);
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
