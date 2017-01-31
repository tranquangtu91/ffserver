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
		FFHttpServer.logger.debug(String.format("RemoveDeviceHttpHandler: %s", arg0.getRequestURI().toString()));
		// parse request
		Map<String, Object> parameters = new HashMap<String, Object>();
        String query = arg0.getRequestURI().getRawQuery();
        Utils.parseQuery(query, parameters);
        
        String response = "";
        String msg = "";
        Object reg_str = parameters.get("reg_str");
        Boolean result = false;
        
    	if (reg_str != null) {
    		try {
				DbUtils.RemoveDevice((String) reg_str);
				MainApplication.ff_server.removeRegDevice((String) reg_str);
				result = true;
				msg = "Success";
			} catch (Exception e) {
				msg = e.getMessage();
			}
    	} else {
    		msg = "Request Params Error";
    	}
    	
    	response = JSONEncoder.genGenericResponse((String) reg_str, result, msg);	
		FFHttpServer.logger.debug(String.format("RemoveDeviceHttpHandler: %s", response));

        Utils.sendResponse(arg0, response);
	}
}
