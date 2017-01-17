/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ffserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.mycompany.server.FFRequest;
import com.mycompany.server.FFServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 *
 * @author TuTQ
 */
public class MainApplication {
    public static void main(String[] args) throws InterruptedException, IOException {
        final FFServer ff_server = new FFServer();
        ff_server.port = 8888;
        ff_server.Start();
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new HttpHandler() {
        	@Override
        	public void handle(HttpExchange arg0) throws IOException {
        		
        		String req_str = "server.createContext";
        		ByteBuf bb = Unpooled.buffer(req_str.length()).writeBytes(req_str.getBytes());
        		FFRequest req = new FFRequest("device01", 0x01, bb, 1000);
        		
        		ff_server.addRegToQueue(req);
        		
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
        });
        server.setExecutor(null);
        server.start();
    }
}
