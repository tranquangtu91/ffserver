package com.mycompany.httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.sun.net.httpserver.HttpServer;

public class FFHttpServer {
	public static Logger logger = Logger.getLogger(FFHttpServer.class.getName());
	
	int port;
	HttpServer server;
	
	public FFHttpServer(int port) throws IOException {
		PropertyConfigurator.configure("log4j.properties");
		
		this.port = port;
		server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/dout", new DOutHttpHandler());
        server.createContext("/din", new DInHttpHandler());
        server.createContext("/ain", new AInHttpHandler());
        server.createContext("/get_device_list", new GetDeviceListHttpHandler());
        server.createContext("/create_device", new CreateDeviceHttpHandler());
        server.createContext("/update_device", new UpdateDeviceHttpHandler());
        server.createContext("/remove_device", new RemoveDeviceHttpHandler());
        server.createContext("/create_user", new CreateUserHttpHandler());
        server.createContext("/login", new LoginHttpHandler());
        server.createContext("/logout", new LogoutHttpHandler());
        server.setExecutor(Executors.newFixedThreadPool(20));
	}
	
	public void Start() {
        server.start();
        logger.info("ff_http_server module started");
	}
}
