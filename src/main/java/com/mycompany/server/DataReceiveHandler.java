/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

/**
 *
 * @author TuTQ
 */
public class DataReceiveHandler extends ChannelInboundHandlerAdapter {
    public String reg_str;

    public DataReceiveHandler() {
        reg_str = "";
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf bb = (ByteBuf)msg;
        if (reg_str.equals("")) {
            while (bb.isReadable()) {
                reg_str += (char)bb.readByte();
            }
        } else {
            System.out.print(String.format("%s received: ", reg_str));
            while (bb.isReadable()) {
                System.out.print(String.format("%02X ", bb.readByte()));
            }
            System.out.println("");
        }
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state() == IdleState.ALL_IDLE) {
                System.out.println(String.format("%s - idle state", reg_str));
            }
        }
    }
    
}
