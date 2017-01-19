package com.mycompany.httpserver;

import java.io.IOException;
import java.io.OutputStream;

import com.mycompany.ffserver.MainApplication;
import com.mycompany.server.FFRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RootHttpHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		String req_str = "?";
		ByteBuf bb = Unpooled.buffer(req_str.length()).writeBytes(req_str.getBytes());
		FFRequest req = new FFRequest("device01", 0x01, bb, 5000);
		
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

		String response = "";
		if (req.have_response) {
			while (req.response.isReadable()) {
				response += (char)req.response.readByte();
			}
		} else {
			response = "Timeout";
		}
		
		arg0.sendResponseHeaders(200, response.length());
        OutputStream os = arg0.getResponseBody();
        os.write(response.getBytes());
        os.close();
	}
}
