/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.device;

import com.mycompany.server.ServerHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 *
 * @author TuTQ
 */
public class FFDevice {
    public SocketChannel soc;
    ServerHandler server_handler;
    IdleStateHandler idle_state_handler;
    public long connect_time;
    
    boolean is_closed = false;

    public FFDevice(SocketChannel ch) {
        this.soc = ch;
        connect_time = System.currentTimeMillis();
        server_handler = new ServerHandler();
        idle_state_handler = new IdleStateHandler(0, 0, 60);
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(idle_state_handler);
        pipeline.addLast(server_handler);
        
        ChannelFuture f = soc.closeFuture();
        f.addListener((ChannelFutureListener) new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                is_closed = true;
                System.err.println(String.format("%s disconnected", server_handler.reg_str));
            }
        });
    }
    
    public String getRegStr() {
        return server_handler.reg_str;
    }
    
    public boolean isClosed() {
        return is_closed;
    }
    
    public void Close() {
        System.out.println("com.mycompany.device.FFDevice.close()");
        soc.close();
    }
}
