/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ffserver;

import com.mycompany.database.DbUtils;
import com.mycompany.device.FFDevice;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author TuTQ
 */
public class FFServer {
	public static Logger logger = Logger.getLogger(FFServer.class.getName());
	
    public int port;
    public static int max_req_queue_size = 2;
    
    public FFDevInfo dev_info;
    public List<FFDevice> device_lst;
    
    List<FFRequest> req_lst;

    public FFServer() {
    	PropertyConfigurator.configure("log4j.properties");
    	
        this.port = 9100;
        
        dev_info = new FFDevInfo(50);
        
        device_lst = new ArrayList<FFDevice>(50);
        
        req_lst = new ArrayList<FFRequest>(max_req_queue_size);
    }
    
    public void Start() throws InterruptedException, SQLException, ClassNotFoundException {
    	
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>(){
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                device_lst.add(new FFDevice(ch));
            }
        });
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.bind(port).sync();
        
        Thread t_check_reg_device = new Thread(new DevManTask());
        t_check_reg_device.start();  
        
        ResultSet rs = DbUtils.getDeviceList();
        String reg_str = "";
        while (rs.next()) {
        	reg_str = rs.getString("regs");
        	addRegDevice(reg_str);
        	DbUtils.updateAllToOffline();
        }
        
        logger.info("ff_server module started");
    }

    public void addRegDevice(String reg_str) {
    	logger.debug(String.format("add reg_str '%s' to reg_lst", reg_str));
        dev_info.addRegStr(reg_str);
    }
    
    public void removeRegDevice(String reg_str) {
    	logger.debug(String.format("remove reg_str '%s' to reg_lst", reg_str));
        dev_info.removeRegStr(reg_str);
    }
    
    public void addReqToQueue(FFRequest req) {
    	if (dev_info.isOnline(req.reg_str)) {
    		if (req_lst.size() >= max_req_queue_size) {
    			req.response.writeBytes("req_queue is limited".getBytes());
    			req.have_response = true;
    		} else {
	    		req_lst.add(req);
	    		logger.info(String.format("req_lst count: %d", req_lst.size()));
    		}
    	} else {
    		req.response.writeBytes("Device is offline".getBytes());
    		req.have_response = true;
    	}
    }
    
    public Boolean checkOnline(String reg_str) {
    	return dev_info.isOnline(reg_str);
    }

    public class DevManTask implements Runnable {
    	@Override
    	public void run() {
    		FFDevice ff_device;
        	FFRequest ff_request;
        	int dev_count = 0;
        	int req_count = 0;
            
            while (true) {
            	
            	dev_count = device_lst.size();
            	
            	for (int i = dev_count - 1; i >= 0; i --)
            	{
            		ff_device = (FFDevice)device_lst.get(i);
            		
            		if (ff_device.isClosed()) {
            			device_lst.remove(i);
            			logger.info(String.format("remove device with reg_str '%s' from device_lst", ff_device.getRegStr()));
            			continue;
            		}
            		
            		if (ff_device.getRegStr().isEmpty()) {
            			if (ff_device.connect_time + 5000 < System.currentTimeMillis()) {
            				ff_device.Close();
            				logger.info("close connection of un-reg device");
            			}
        				continue;
            		}
            		
            		if (!dev_info.isAvailable(ff_device.getRegStr())) {
            			ff_device.Close();
            			logger.info(String.format("close old connection of device that have reg_str '%s'", ff_device.getRegStr()));
            			continue;
            		}
            		
        			dev_info.lockRegStr(ff_device.getRegStr());
        			
        			req_count = req_lst.size();
        			for (int j = 0; j < req_count; j ++) {
        				ff_request = req_lst.get(j);
        				if (ff_request.reg_str.equals(ff_device.getRegStr()) && ff_device.req == null) {
        					logger.info(String.format("send req to %s", ff_device.getRegStr()));
        					ff_device.req = ff_request;
        					req_lst.remove(j);
        					break;
        				}
        			}
        			
        			ff_device.Process();
            	}
            	
            	dev_info.freeAllRegStr();
                
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                	System.err.println(ex.getMessage());
                }
            }
    	}
    }
}
