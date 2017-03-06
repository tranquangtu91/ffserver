package com.mycompany.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

public class Utils {
	public static Map<String, SessionInfo> user_manager = new HashMap<String, SessionInfo>();
	
	public static int checkSessionInfo(SessionInfo session_info, String session_id) {
		int code = 0;
		if (session_info == null) {
			code = -100;
//			msg = "De nghi dang nhap";
    	} else if (!session_info.session_id.equals(session_id)) {
    		code = -101;
//			msg = "Tai khoan bi dang nhap tai mot noi khac, de nghi dang nhap lai";
		} else if (session_info.expiry_time < System.currentTimeMillis()) {
			code = -102;
//			msg = "Het phien lam viec, de nghi dang nhap lai";
		} 
		return code;
	}
	
	public static void sendResponse(HttpExchange arg0, String response) throws IOException {		
		arg0.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		arg0.getResponseHeaders().add("Content-Type", "application/json");
        arg0.sendResponseHeaders(200, response.length());
        OutputStream os = arg0.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
	}
	
	public static void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

		if (query != null) {
			String pairs[] = query.split("[&]");
			for (String pair : pairs) {
				String param[] = pair.split("[=]");
				String key = null;
				String value = null;
				if (param.length > 0) {
					key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
				}

				if (param.length > 1) {
					value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
              	}

				if (parameters.containsKey(key)) {
					Object obj = parameters.get(key);
					if (obj instanceof List<?>) {
						@SuppressWarnings("unchecked")
						List<String> values = (List<String>) obj;
						values.add(value);

					} else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
					}
				} else {
					parameters.put(key, value);
				}
			}
		}
	}
}
