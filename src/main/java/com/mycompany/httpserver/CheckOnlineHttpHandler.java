package com.mycompany.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.main.MainApplication;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CheckOnlineHttpHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		// parse request
		Map<String, Object> parameters = new HashMap<String, Object>();
        String query = arg0.getRequestURI().getRawQuery();
        Utils.parseQuery(query, parameters);
        
        String response = "";
    	if (parameters.get("reg_str") != null)
    		response = MainApplication.ff_server.checkOnline(parameters.get("reg_str").toString()) ? "online" : "offline";
    	else {
    		response = "error";
    	}
        
        arg0.sendResponseHeaders(200, response.length());
        OutputStream os = arg0.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
	}
}
