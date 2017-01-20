package com.mycompany.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.ffserver.FFRequest;
import com.mycompany.ffserver.FFRequest.EnumRequestType;
import com.mycompany.main.MainApplication;
import com.mycompany.modbus.ModbusRTU;
import com.mycompany.utils.JSONEncoder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.netty.buffer.Unpooled;

public class DInHttpHandler implements HttpHandler{
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		// TODO Auto-generated method stub
		FFHttpServer.logger.debug(String.format("DInHttpHandler: %s", arg0.getRequestURI().toString()));
		
		// parse request
		Map<String, Object> parameters = new HashMap<String, Object>();
	    String query = arg0.getRequestURI().getRawQuery();
		Utils.parseQuery(query, parameters);
	    
	    String response = "";
	    String msg = "";
	    Boolean result = false;
	    byte di_state = 0x00;
	    
	    Object reg_str = parameters.get("reg_str");
		if (reg_str != null) {
			
			FFRequest req = new FFRequest((String) reg_str, 
					EnumRequestType.Modbus_0x02, 
					Unpooled.copiedBuffer(ModbusRTU.genMsgGetDIn((byte) 0x01, 0x00, 0x08)), 
					5000);
			MainApplication.ff_server.addRegToQueue(req);
			
			while (!req.have_response && 
					(req.request_create_time + req.request_time_out_ms + 5000) > System.currentTimeMillis()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}       
			
			if (req.have_response) {
				if (!req.result) {
					msg = req.response.toString(Charset.defaultCharset());
				}
				else if (req.response.readableBytes() >= 6) {	
					msg = "Success";
					di_state = req.response.getByte(3);
					result = true;
				} else {
					msg = "Error";
				}
			} else {
				msg = "Timeout";
			}
		} else {
			msg = "Request Params Error";
		}
	    
		response = JSONEncoder.genDInResponse((String) reg_str, result, di_state, msg);
		FFHttpServer.logger.debug(String.format("DInHttpHandler: %s", response));
		
	    arg0.sendResponseHeaders(200, response.length());
	    OutputStream os = arg0.getResponseBody();
	    os.write(response.toString().getBytes());
	    os.close();
	}
}
