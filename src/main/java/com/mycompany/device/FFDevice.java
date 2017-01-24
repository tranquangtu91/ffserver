/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.device;

import java.nio.charset.Charset;

import com.mycompany.ffserver.FFRequest;
import com.mycompany.ffserver.FFServer;

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
    SocketChannel soc;
    public long connect_time;
    
    int idle_time_interval_s = 120;
    String reg_str;
    boolean is_closed = false;
    
    ByteBuf data_rcv;
    public FFRequest req;

    public FFDevice(SocketChannel ch) {
    	this.req = null;
        this.soc = ch;
        data_rcv = soc.alloc().buffer(512);
        this.reg_str = "";
        connect_time = System.currentTimeMillis();
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 0, idle_time_interval_s));
        pipeline.addLast(new MessageToByteEncoder<byte[]>() {
        	@Override
        	protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) throws Exception {
        		// TODO Auto-generated method stub
        		out.writeBytes(msg);
        	}
		});
        pipeline.addLast(new ChannelInboundHandlerAdapter() {
        	@Override
        	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        		// TODO Auto-generated method stub
        		ByteBuf bb = (ByteBuf)msg;
                if (reg_str.equals("")) {
                    reg_str = bb.toString(Charset.defaultCharset());
                } else {
                	FFServer.logger.debug(String.format("%s receive: %d bytes", reg_str, bb.readableBytes()));
                	data_rcv.writeBytes(bb);
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
                    	FFServer.logger.debug(String.format("%s in idle state", reg_str));
                    }
                }
            }
        });
        
        ChannelFuture f = soc.closeFuture();
        f.addListener((ChannelFutureListener) new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                is_closed = true;
                FFServer.logger.debug(String.format("%s disconnected", reg_str));
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
        FFServer.logger.debug("com.mycompany.device.FFDevice.close()");
        soc.close();
    }
    
    public void Send(Object data) {
    	if (is_closed) return;
    	soc.writeAndFlush(data);
    }
    
    public void clearDataRcv() {
    	data_rcv.clear();
    }
    
    public void Process() {
    	if (req == null) return;
    	
    	if (!req.is_waiting) {
	    	req.is_waiting = true;
	    	Send(req.request.array());
	    	data_rcv.clear();
    	} else {
    		if (req.request_create_time + req.request_time_out_ms < System.currentTimeMillis()) {
    			req.have_response = true;
    	    	req.response.writeBytes("Device online but not response".getBytes());
    	    	req = null;
    	    	return;
    		}
    		
    		switch (req.request_type) {
				case Modbus_0x05:
		    		if (data_rcv.readableBytes() >= 8) {
		    	    	req.have_response = true;
		    	    	req.response = data_rcv;
		    	    	req.result = true;
		    	    	req = null;
		    	    	return;
		    		}
					break;
				case Modbus_0x04:
					if (data_rcv.readableBytes() >= 37) {
						req.have_response = true;
		    	    	req.response = data_rcv;
		    	    	req.result = true;
		    	    	req = null;
		    	    	return;
					}
					break;
				case Modbus_0x02:
					if (data_rcv.readableBytes() >= 6) {
						req.have_response = true;
		    	    	req.response = data_rcv;
		    	    	req.result = true;
		    	    	req = null;
		    	    	return;
					}
					break;
				default:
					break;
			}
    	}
    }
}
