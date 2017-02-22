package com.mycompany.httpserver;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.database.DbUtils;
import com.mycompany.httpserver.SessionInfo.EnumPermission;
import com.mycompany.utils.JSONEncoder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class LoginHttpHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange arg0) throws IOException {
		// TODO Auto-generated method stub
		FFHttpServer.logger.debug(String.format("%s -> %s", arg0.getRemoteAddress(), arg0.getRequestURI().toString()));
		// parse request
		Map<String, Object> parameters = new HashMap<String, Object>();
		int contentLength = Integer.parseInt(arg0.getRequestHeaders().getFirst("Content-length"));
		byte[] data = new byte[contentLength];
		arg0.getRequestBody().read(data);
		String query = new String(data);
        Utils.parseQuery(query, parameters);
        
        String response = "";
        String msg = "";
        String session_id = "";
        int permission_db = 0;
        Boolean result = false;
        ResultSet rs = null;
        
        Object username = parameters.get("username");
        Object password = parameters.get("password");
        if (username != null && password != null) {
	    	try {
				rs = DbUtils.getUser((String) username);
				if (rs.first()){
					byte[] password_db = rs.getBytes("password");
					permission_db = rs.getInt("permission");
					byte[] password_md5 = DbUtils.generateMD5Arr((String)username + (String)password + permission_db);
					if (Arrays.equals(password_db, password_md5)) {
						session_id = String.format("%d%d", (int)(Math.random()*65535), (int)(Math.random()*65535));
						
						SessionInfo session_info = new SessionInfo();
						session_info.session_id = session_id;
						session_info.remote_addr = arg0.getRemoteAddress();
						switch (permission_db) {
							case 1:
								session_info.permission = EnumPermission.Admintrator;
								break;
							default:
								session_info.permission = EnumPermission.Guest;
								break;
						}
						
						FFHttpServer.user_manager.put((String) username, session_info);
						
						msg = "Success";
						result = true;
					} else {
						msg = "Username not Exist / Password is Invalid";
					}
				} else {
					msg = "Username not Exist / Password is Invalid";
				}
			} catch (Exception e) {
				msg = e.getMessage();
			}
        } else {
        	msg = "Request Params Error";
        }
    	
    	response = JSONEncoder.genUserLoginResponse((String) username, result, msg, session_id, permission_db);
		FFHttpServer.logger.debug(String.format("%s <- %s", arg0.getRemoteAddress(), response));

        Utils.sendResponse(arg0, response);
	}
}
