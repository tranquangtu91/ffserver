package com.mycompany.httpserver;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.database.DbUtils;
import com.mycompany.utils.JSONEncoder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GetDeviceListHttpHandler implements HttpHandler{
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		// TODO Auto-generated method stub
		FFHttpServer.logger.debug(String.format("GetDeviceListHttpHandler: %s", arg0.getRequestURI().toString()));
		// parse request
		Map<String, Object> parameters = new HashMap<String, Object>();
        String query = arg0.getRequestURI().getRawQuery();
        Utils.parseQuery(query, parameters);
        
        String response = "";
        String msg = "";
        Boolean result = false;
        ResultSet rs = null;
        
    	try {
			rs = DbUtils.getDeviceList();
			msg = "Success";
			result = true;
		} catch (Exception e) {
			msg = e.getMessage();
		}
    	
    	response = JSONEncoder.genDeviceListResponse(result, rs, msg);
		FFHttpServer.logger.debug(String.format("GetDeviceListHttpHandler: %s", response));

        Utils.sendResponse(arg0, response);
	}
}
