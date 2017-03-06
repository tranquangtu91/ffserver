package com.mycompany.httpserver;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mycompany.database.DbUtils;
import com.mycompany.httpserver.SessionInfo.EnumPermission;
import com.mycompany.utils.JSONEncoder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GetDeviceListHttpHandler implements HttpHandler{
	@SuppressWarnings("unchecked")
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		// TODO Auto-generated method stub
		long start_time = System.currentTimeMillis();
		
		FFHttpServer.logger.debug(String.format("%s -> %s", arg0.getRemoteAddress(), arg0.getRequestURI().toString()));
		// parse request
		Map<String, Object> parameters = new HashMap<String, Object>();
        String query = arg0.getRequestURI().getRawQuery();
        Utils.parseQuery(query, parameters);
        
        String response = "";
        String msg = "";
        Boolean result = false;
        ResultSet rs = null;
        int code = 0;
        JSONArray device_info = new JSONArray();
        
        Object username = parameters.get("username");
        Object session_id = parameters.get("session_id");
        
        if (username != null && session_id != null) {
    		SessionInfo session_info = Utils.user_manager.get(username);
    		code = Utils.checkSessionInfo(session_info, (String) session_id);
        	if (code == -1000) {
    			msg = "De nghi dang nhap";
        	} else if (code == -1001) {
    			msg = "Tai khoan bi dang nhap tai mot noi khac, de nghi dang nhap lai";
    		} else if (code == -1002) {
    			msg = "Het phien lam viec, de nghi dang nhap lai";
    		} else {
    			try {
	    			if (session_info.permission == EnumPermission.Admintrator) {
	    				rs = DbUtils.getDeviceList();
	    			} else {
	    				rs = DbUtils.getDeviceList(session_info.user_id);
	    			}
	    			
	    			session_info.device_lst.clear();
	    			while (rs.next()) {
						JSONObject jobj = new JSONObject();
						String rsg_str = rs.getString("name");
						jobj.put("name", rsg_str);
						jobj.put("id", rs.getInt("id"));
						jobj.put("regs", rs.getString("regs"));
						jobj.put("onl", rs.getBoolean("online"));
						jobj.put("conn_lt", rs.getString("connect_last_time"));
						jobj.put("lat", rs.getDouble("latitude"));
						jobj.put("lng", rs.getDouble("longitude"));
						jobj.put("desc", rs.getString("description"));
						device_info.add(jobj);
						
						session_info.device_lst.put(rs.getString("id"), rs.getString("regs"));
					}
	    			msg = "Success";
	    			result = true;
    			} catch (Exception e) {
					msg = e.getMessage();
					code = -4;
				}
    		}
        } else {
        	msg = "Request Params Error";
        	code = -1;
        }
    	
    	response = JSONEncoder.genDeviceListResponse(result, device_info, msg, code);
		FFHttpServer.logger.debug(String.format("%s <- %dms: %s", arg0.getRemoteAddress(), System.currentTimeMillis() - start_time, response));

        Utils.sendResponse(arg0, response);
	}
}
