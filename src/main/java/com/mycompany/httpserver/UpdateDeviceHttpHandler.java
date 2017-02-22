package com.mycompany.httpserver;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.database.DbUtils;
import com.mycompany.main.MainApplication;
import com.mycompany.utils.JSONEncoder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class UpdateDeviceHttpHandler implements HttpHandler{
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
        Boolean result = false;
        
        Object reg_str = parameters.get("reg_str");
        Object name = parameters.get("name");
        Object desc = parameters.get("desc");
        Object lat = parameters.get("lat");
        Object lng = parameters.get("lng");
        if (reg_str != null && name != null) {
	    	try {
	    		ResultSet rs = DbUtils.getDeviceInfo((String) name);
	    		String old_regs = null;
	    		if (rs.next()) {
	    			old_regs = rs.getString("regs");
	    		}
	    		if (old_regs != null) {
					DbUtils.updateDevice((String)name, 
							(String) reg_str, 
							(String) desc, 
							lat == null ? 0: Double.parseDouble((String)lat), 
							lng == null ? 0 : Double.parseDouble((String)lng));
					if (!old_regs.equals(reg_str)) {
						MainApplication.ff_server.removeRegDevice(old_regs);
						MainApplication.ff_server.addRegDevice((String) reg_str);
						DbUtils.updateOnlineState((String) reg_str, false);
					}
					msg = "Success";
					result = true;
	    		}
	    		else 
	    		{
	    			msg = "Device does not exist";
	    		}
			} catch (Exception e) {
				msg = e.getMessage();
			}
        } else {
        	msg = "Request Params Error";
        }
    	
    	response = JSONEncoder.genGenericDeviceResponse((String) reg_str, result, msg);
		FFHttpServer.logger.debug(String.format("%s <- %s", arg0.getRemoteAddress(), response));

        Utils.sendResponse(arg0, response);
	}
}
