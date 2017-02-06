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
		FFHttpServer.logger.debug(String.format("CreateDeviceHttpHandler: %s", arg0.getRequestURI().toString()));
		// parse request
		Map<String, Object> parameters = new HashMap<String, Object>();
		int contentLength = Integer.parseInt(arg0.getRequestHeaders().getFirst("Content-length"));
		byte[] data = new byte[contentLength];
		arg0.getRequestBody().read(data);
		String query = new String(data);
        Utils.parseQuery(query, parameters);
        
        String response = "";
        String msg = "";
        Boolean result = false;
        
        Object reg_str = parameters.get("reg_str");
        Object name = parameters.get("name");
        Object desc = parameters.get("desc");
        Object lat = parameters.get("lat");
        Object lng = parameters.get("lng");
        if (reg_str != null && name != null) {
	    	try {
				DbUtils.createDevice((String)name, (String) reg_str, 
								(String) desc, lat == null ? 0: Double.parseDouble((String)lat), 
								lng == null ? 0 : Double.parseDouble((String)lng));
				MainApplication.ff_server.addRegDevice((String) reg_str);
				msg = "Success";
				result = true;
			} catch (Exception e) {
				msg = e.getMessage();
			}
        } else {
        	msg = "Request Params Error";
        }
    	
    	response = JSONEncoder.genGenericResponse((String) reg_str, result, msg);
		FFHttpServer.logger.debug(String.format("CreateDeviceHttpHandler: %s", response));

        Utils.sendResponse(arg0, response);
	}
}
