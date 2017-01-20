package com.mycompany.httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

public class FFHttpServer {
	int port;
	HttpServer server;
	
	public FFHttpServer(int port) throws IOException {
		this.port = port;
		server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/check_online", new CheckOnlineHttpHandler());
        server.createContext("/dout", new DOutHttpHandler());
        server.createContext("/din", new DInHttpHandler());
        server.createContext("/ain", new AInHttpHandler());
        server.setExecutor(Executors.newFixedThreadPool(20));
	}
	
	public void Start() {
        server.start();
        System.out.println("ff_http_server start");
	}
}
