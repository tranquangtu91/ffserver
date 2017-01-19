/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ffserver;

import com.mycompany.device.FFDevice;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author TuTQ
 */
public class FFServer {
    public int port;
    public static int max_req_queue_size = 2;
    
    class DevInfo {
    	int size = 50;
    	
        List<String> reg_str_lst;
        List<Boolean> in_use_flags;
        List<Boolean> is_online;

        public DevInfo(int size) {
        	this.size = size;
            this.in_use_flags = new ArrayList<Boolean>(size);
            this.reg_str_lst = new ArrayList<String>(size);
            this.is_online = new ArrayList<Boolean>(size);
        }
        
        public void addRegStr(String reg_str) {
            if (!reg_str_lst.contains(reg_str)) {
                reg_str_lst.add(reg_str);
                in_use_flags.add(false);
                is_online.add(false);
            }
        }
        
        public void removeRegStr(String reg_str) {
            int id = reg_str_lst.indexOf(reg_str);
            if (id != -1) {
                reg_str_lst.remove(id);
                in_use_flags.remove(id);
                is_online.remove(id);
            }
        }
        
        public void lockRegStr(String reg_str) {
            int id = reg_str_lst.indexOf(reg_str);
            if (id != -1) {
                in_use_flags.set(id, true);
                is_online.set(id, true);
            }
        }
        
        public int getIndexRegStr(String reg_str) {
            return reg_str_lst.indexOf(reg_str);
        }
        
        //-1: not exist; 0: available; 1: in use
        public Boolean isAvailable(String reg_str) {
            int id = reg_str_lst.indexOf(reg_str);
            if (id != -1) {
                return !in_use_flags.get(id);
            }
            return false;
        }
        
        public void freeAllRegStr() {
        	int flag_count = in_use_flags.size();
        	for (int i = 0; i < flag_count; i ++) {
        		if (!in_use_flags.get(i)) is_online.set(i, false);
        		in_use_flags.set(i, false);
        	}
        }
        
        public Boolean isOnline(String reg_str) {
        	int id = reg_str_lst.indexOf(reg_str);
        	if (id != -1) {
        		return is_online.get(id);
        	}
        	return false;
        }
    }
    
    public DevInfo dev_info;
    public List<FFDevice> device_lst;
    
    List<FFRequest> req_lst;

    public FFServer() {
        this.port = 9100;
        
        dev_info = new DevInfo(50);
        dev_info.addRegStr("device01");
        dev_info.addRegStr("device02");
        
        device_lst = new ArrayList<FFDevice>(50);
        
        req_lst = new ArrayList<FFRequest>(max_req_queue_size);
    }
    
    public void Start() throws InterruptedException {
    	
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
        
        Thread t_check_reg_device;
        t_check_reg_device = new Thread(() -> {
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
            			System.out.println(String.format("remove device with reg_str '%s' from device_lst", ff_device.getRegStr()));
            			continue;
            		}
            		
            		if (ff_device.getRegStr().isEmpty()) {
            			if (ff_device.connect_time + 5000 < System.currentTimeMillis()) {
            				ff_device.Close();
            				System.out.println("close connection of un-reg device");
            			}
        				continue;
            		}
            		
            		if (!dev_info.isAvailable(ff_device.getRegStr())) {
            			ff_device.Close();
            			System.out.println(String.format("close old connection of device that have reg_str '%s'", ff_device.getRegStr()));
            			continue;
            		}
            		
        			dev_info.lockRegStr(ff_device.getRegStr());
        			
        			req_count = req_lst.size();
        			for (int j = 0; j < req_count; j ++) {
        				ff_request = req_lst.get(j);
        				if (ff_request.reg_str.equals(ff_device.getRegStr()) && ff_device.req == null) {
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
        });
        t_check_reg_device.start();  
        
        System.out.println("com.mycompany.server.FFServer.Start()");
    }

    public void addRegToQueue(FFRequest req) {
    	if (dev_info.isOnline(req.reg_str)) {
    		if (req_lst.size() >= max_req_queue_size) {
    			req.response.writeBytes("req_queue is limited".getBytes());
    			req.have_response = true;
    		} else {
	    		req_lst.add(req);
	    		System.out.println(String.format("req_lst count: %d", req_lst.size()));
    		}
    	} else {
    		req.response.writeBytes("device is offline".getBytes());
    		req.have_response = true;
    	}
    }
    
    public Boolean checkOnline(String reg_str) {
    	return dev_info.isOnline(reg_str);
    }
}
