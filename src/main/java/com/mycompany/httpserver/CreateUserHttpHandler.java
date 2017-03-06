package com.mycompany.httpserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.database.DbUtils;
import com.mycompany.httpserver.SessionInfo.EnumPermission;
import com.mycompany.utils.JSONEncoder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CreateUserHttpHandler implements HttpHandler{
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		// TODO Auto-generated method stub
		long start_time = System.currentTimeMillis();
		
		FFHttpServer.logger.debug(String.format("%s -> %s", arg0.getRemoteAddress(), arg0.getRequestURI().toString()));
		// parse request
		Map<String, Object> body_parameters = new HashMap<String, Object>();
		Map<String, Object> uri_parameters = new HashMap<String, Object>();
		int contentLength = Integer.parseInt(arg0.getRequestHeaders().getFirst("Content-length"));
		byte[] data = new byte[contentLength];
		arg0.getRequestBody().read(data);
		String query = new String(data);
        Utils.parseQuery(query, body_parameters);
        query = arg0.getRequestURI().getRawQuery();
        Utils.parseQuery(query, uri_parameters);
        
        String response = "";
        String msg = "";
        int code = 0;
        Boolean result = false;
        
        Object username = uri_parameters.get("username");
        Object session_id = uri_parameters.get("session_id");
        Object body_username = body_parameters.get("username");
        Object body_password = body_parameters.get("password");
        Object body_permission = body_parameters.get("permission");
        if (username != null && session_id != null && body_username != null && body_password != null && body_permission != null) {
    		SessionInfo session_info = Utils.user_manager.get(username);
    		code = Utils.checkSessionInfo(session_info, (String) session_id);
        	if (code == -1000) {
    			msg = "De nghi dang nhap";
        	} else if (code == -1001) {
    			msg = "Tai khoan bi dang nhap tai mot noi khac, de nghi dang nhap lai";
    		} else if (code == -1002) {
    			msg = "Het phien lam viec, de nghi dang nhap lai";
    		} else if (session_info.permission != EnumPermission.Admintrator) {
    			code = -4;
    			msg = "Khong co quyen tao tai khoan";
    		} else {
    			try {
					DbUtils.createUser((String)body_username, (String)body_password, Integer.parseInt((String) body_permission));
					msg = "Success";
					result = true;
    			} catch (Exception e) {
    				msg = e.getMessage();
    				code = -5;
    			}
    		}
        } else {
        	msg = "Request Params Error";
        	code = -1;
        }
    	
    	response = JSONEncoder.genGenericResponse(result, msg, code);
		FFHttpServer.logger.debug(String.format("%s <- %dms: %s", arg0.getRemoteAddress(), System.currentTimeMillis() - start_time, response));

        Utils.sendResponse(arg0, response);
	}
}
