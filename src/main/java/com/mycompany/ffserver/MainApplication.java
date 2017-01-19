/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ffserver;

import java.io.IOException;

import com.mycompany.httpserver.FFHttpServer;
import com.mycompany.server.FFServer;

/**
 *
 * @author TuTQ
 */
public class MainApplication {
	public static FFServer ff_server;
	static FFHttpServer ff_http_server;
	
    public static void main(String[] args) throws InterruptedException, IOException {
        ff_server = new FFServer();
        ff_server.port = 8888;
        ff_server.Start();
        
        ff_http_server = new FFHttpServer(8000);
        ff_http_server.Start();
    }
}
