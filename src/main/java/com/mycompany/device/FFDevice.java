/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.device;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;

/**
 *
 * @author TuTQ
 */
public class FFDevice {
    public SocketChannel soc;
    public long connect_time;
    public String reg_str;
    
    boolean is_closed = false;

    public FFDevice(SocketChannel ch) {
        this.soc = ch;
        this.reg_str = "";
        connect_time = System.currentTimeMillis();
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 0, 5));
        pipeline.addLast(new MessageToByteEncoder<Object>() {
        	@Override
        	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        		// TODO Auto-generated method stub
        		out.writeBytes(msg.toString().getBytes());
        	}
		});
        pipeline.addLast(new ChannelInboundHandlerAdapter() {
        	@Override
        	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        		// TODO Auto-generated method stub
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
        		// TODO Auto-generated method stub
        		super.exceptionCaught(ctx, cause);
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
        });
        
        ChannelFuture f = soc.closeFuture();
        f.addListener((ChannelFutureListener) new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                is_closed = true;
                System.out.println(String.format("%s disconnected", reg_str));
            }
        });
    }
    
    public String getRegStr() {
        return reg_str;
    }
    
    public boolean isClosed() {
        return is_closed;
    }
    
    public void Close() {
        System.out.println("com.mycompany.device.FFDevice.close()");
        soc.close();
    }
    
    public void Send(Object data) {
    	if (is_closed) return;
    	soc.writeAndFlush(data);
    }
}
