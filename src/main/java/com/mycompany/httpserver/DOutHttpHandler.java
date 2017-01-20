package com.mycompany.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.ffserver.FFRequest;
import com.mycompany.ffserver.FFRequest.EnumRequestType;
import com.mycompany.main.MainApplication;
import com.mycompany.modbus.ModbusRTU;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.netty.buffer.Unpooled;

public class DOutHttpHandler implements HttpHandler{
	
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		// TODO Auto-generated method stub
		// parse request
		Map<String, Object> parameters = new HashMap<String, Object>();
	    String query = arg0.getRequestURI().getRawQuery();
		Utils.parseQuery(query, parameters);
	    
	    String response = "";
	    Object reg_str = parameters.get("reg_str");
	    Object dout = parameters.get("dout");
	    Object state = parameters.get("state");
		if (reg_str != null && dout != null && state != null) {
			
			FFRequest req = new FFRequest((String) reg_str, 
					EnumRequestType.Modbus_0x05, 
					Unpooled.copiedBuffer(ModbusRTU.GenMsg_SetDo((byte) 0x01, Integer.parseInt((String) dout), Boolean.parseBoolean((String)state))), 
					5000);
			MainApplication.ff_server.addRegToQueue(req);
			
			while (!req.have_response && 
					(req.request_create_time + req.request_time_out_ms) > System.currentTimeMillis()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}       
			
			if (req.have_response) {
				if (!req.result) {
					response = req.response.toString(Charset.defaultCharset());
				}
				else if (req.response.equals(req.request)) {
					response = "Success";
				} else {
					response = "Error";
				}
			} else {
				response = "Timeout";
			}
		} else {
			response = "Request Params Error";
		}
	    
	    arg0.sendResponseHeaders(200, response.length());
	    OutputStream os = arg0.getResponseBody();
	    os.write(response.toString().getBytes());
	    os.close();
	}
}
