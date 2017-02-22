package com.mycompany.httpserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.main.MainApplication;
import com.mycompany.utils.JSONEncoder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CheckOnlineHttpHandler implements HttpHandler {
	
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		FFHttpServer.logger.debug(String.format("%s -> %s", arg0.getRemoteAddress(), arg0.getRequestURI().toString()));
		// parse request
		Map<String, Object> parameters = new HashMap<String, Object>();
        String query = arg0.getRequestURI().getRawQuery();
        Utils.parseQuery(query, parameters);
        
        String response = "";
        String msg = "";
        Object reg_str = parameters.get("reg_str");
        
    	if (reg_str != null)
    		msg = MainApplication.ff_server.checkOnline(parameters.get("reg_str").toString()) ? "online" : "offline";
    	else {
    		msg = "Request Params Error";
    	}
    	
    	response = JSONEncoder.genGenericDeviceResponse((String) reg_str, true, msg);	
		FFHttpServer.logger.debug(String.format("%s <- %s", arg0.getRemoteAddress(), response));
        
		Utils.sendResponse(arg0, response);
	}
}
