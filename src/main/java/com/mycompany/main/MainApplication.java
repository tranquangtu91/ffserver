/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.main;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.mycompany.ffserver.FFServer;
import com.mycompany.httpserver.FFHttpServer;

/**
 *
 * @author TuTQ
 */
public class MainApplication {
	static Logger logger = Logger.getLogger(MainApplication.class.getName());
	
	public static FFServer ff_server;
	static FFHttpServer ff_http_server;
	
    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException, SQLException {
    	PropertyConfigurator.configure("log4j.properties");
    	
        ff_server = new FFServer();
        ff_server.port = Integer.parseInt(args[0]);
        ff_server.Start();
        
        ff_http_server = new FFHttpServer(Integer.parseInt(args[1]));
        ff_http_server.Start();
    }
}
