package com.mycompany.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.ffserver.FFRequest;
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
	    Object dout = parameters.get("do");
	    Object state = parameters.get("state");
		if (reg_str != null && dout != null && state != null) {
			
			FFRequest req = new FFRequest((String) reg_str, 0x01, 
					Unpooled.copiedBuffer(ModbusRTU.GenMsg_SetDo((byte) 0x01, 0x00, state.equals("0") ? false : true)), 5000);
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
				while (req.response.isReadable()) {
					response += (char)req.response.readByte();
				}
			} else {
				response = "Timeout";
			}
		} else {
			response = "error";
		}
	    
	    arg0.sendResponseHeaders(200, response.length());
	    OutputStream os = arg0.getResponseBody();
	    os.write(response.toString().getBytes());
	    os.close();
	}
}
