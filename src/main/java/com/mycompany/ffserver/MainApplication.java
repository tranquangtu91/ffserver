/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ffserver;

import com.mycompany.server.FFServer;

/**
 *
 * @author TuTQ
 */
public class MainApplication {
    public static void main(String[] args) throws InterruptedException {
        FFServer ff_server = new FFServer();
        ff_server.port = 8888;
        ff_server.Start();
    }
}
