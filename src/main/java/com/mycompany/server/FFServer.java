/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.server;

import com.mycompany.device.FFDevice;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TuTQ
 */
public class FFServer {
    public int port;
    
    class DevRegInfo {
        List<String> reg_str_lst;
        List<Integer> in_use_state_lst;

        public DevRegInfo() {
            this.in_use_state_lst = new ArrayList<Integer>(50);
            this.reg_str_lst = new ArrayList<String>(50);
        }
        
        public void addRegStr(String reg_str) {
            if (!reg_str_lst.contains(reg_str)) {
                reg_str_lst.add(reg_str);
                in_use_state_lst.add(-1);
            }
        }
        
        public void removeRegStr(String reg_str) {
            int id = reg_str_lst.indexOf(reg_str);
            if (id != -1) {
                reg_str_lst.remove(id);
                in_use_state_lst.remove(id);
            }
        }
        
        public void freeRegStr(String reg_str) {
            int id = reg_str_lst.indexOf(reg_str);
            if (id != -1) {
                in_use_state_lst.set(id, -1);
            }
        }
        
        public void lockRegStr(String reg_str, int index) {
            int id = reg_str_lst.indexOf(reg_str);
            if (id != -1) {
                in_use_state_lst.set(id, index);
            }
        }
        
        public int getIndexRegStr(String reg_str) {
            return reg_str_lst.indexOf(reg_str);
        }
        
        //-1: not exist; 0: avaiable; 1: in use
        public int isAvaiable(String reg_str) {
            int id = reg_str_lst.indexOf(reg_str);
            if (id != -1) {
                return (int)in_use_state_lst.get(id) == -1 ? 0:1;
            }
            return -1;
        }
    }
    
    public DevRegInfo dev_reg_info;
    public List<FFDevice> device_lst;
    public List<FFDevice> device_lst_wait_reg;

    public FFServer() {
        this.port = 9100;
        
        dev_reg_info = new DevRegInfo();
        dev_reg_info.addRegStr("device01");
        dev_reg_info.addRegStr("device02");
        
        device_lst = new ArrayList<FFDevice>(50);
        device_lst_wait_reg = new ArrayList<FFDevice>(50);
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
                final FFDevice ff_device = new FFDevice(ch);
                device_lst_wait_reg.add(ff_device);
            }
        });
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.bind(port).sync();
        
        Thread t_check_reg_device;
        t_check_reg_device = new Thread(() -> {
            FFDevice ff_device;
            
            while (true) {
                for (int i = 0; i < device_lst_wait_reg.size(); i ++) {
                    ff_device = (FFDevice)device_lst_wait_reg.get(i);
                    if (ff_device.isClosed()) {
                        device_lst_wait_reg.remove(ff_device);
                        i--;
                        System.out.println("remove unknown conn from device_lst_wait_reg");
                    } else if (ff_device.connect_time + 5000 < System.currentTimeMillis() && ff_device.getRegStr().isEmpty()) {
                        ff_device.Close();
                    }
                    
                    if (!ff_device.getRegStr().isEmpty()) {
                        int result = dev_reg_info.isAvaiable(ff_device.getRegStr());
                        if (result == 0) {
                            device_lst_wait_reg.remove(ff_device);
                            device_lst.add(ff_device);
                            dev_reg_info.lockRegStr(ff_device.getRegStr(), device_lst.size()-1);
                            System.out.println(String.format("remove %s from device_lst_wait_reg and add to device_lst", ff_device.getRegStr()));
                        } else if (result == 1) {
                            result = dev_reg_info.getIndexRegStr(ff_device.getRegStr());
                            ((FFDevice)device_lst.get(result)).Close();
                        }
                    }
                }
                
                for (int i = 0; i < device_lst.size(); i ++) {
                    ff_device = (FFDevice)device_lst.get(i);
                    if (ff_device.isClosed()) {
                        device_lst.remove(ff_device);
                        dev_reg_info.freeRegStr(ff_device.getRegStr());
                        i--;
                        System.err.println(String.format("remove %s from device_lst", ff_device.getRegStr()));
                    }
                }
                
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FFServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t_check_reg_device.start();  
        
        System.out.println("com.mycompany.server.FFServer.Start()");
    }
}
