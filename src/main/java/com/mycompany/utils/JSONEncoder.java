package com.mycompany.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

public class JSONEncoder {
	
	@SuppressWarnings("unchecked")
	public static String genGenericDeviceResponse(String reg_str, Boolean result, String msg) {
		
		final JSONObject obj = new JSONObject();

		obj.put("reg_str", reg_str);
		obj.put("msg", msg);
		obj.put("result", result);
		
		return obj.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	public static String genGenericUserResponse(String username, Boolean result, String msg, int code) {
		
		final JSONObject obj = new JSONObject();

		obj.put("username", username);
		obj.put("msg", msg);
		obj.put("result", result);
		obj.put("code", code);
		
		return obj.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	public static String genUserLoginResponse(String username, Boolean result, String msg, String session_id, int permission) {
		
		final JSONObject obj = new JSONObject();

		obj.put("username", username);
		obj.put("msg", msg);
		obj.put("result", result);
		if (result) {
			obj.put("session_id", session_id);
			obj.put("permision", permission);
		}
		
		return obj.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	public static String genDInResponse(String reg_str, Boolean result, byte di_state, String msg) {
		
		final JSONObject obj = new JSONObject();
		final List<Integer> di_states = new ArrayList<Integer>(8);

		obj.put("reg_str", reg_str);
		obj.put("msg", msg);
		if (result) {
			for (int i = 0 ; i < 8; i ++) {
				if (((di_state >> i) & 0x01) == 0x01) {
					di_states.add(1);
				} else {
					di_states.add(0);
				}
			}
			obj.put("digital_input", di_states);
		}
		obj.put("result", result);
		
		return obj.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	public static String genAInResponse(String reg_str, Boolean result, int[] ai_state, String msg) {
		final JSONObject obj = new JSONObject();
		final List<Integer> ai_states = new ArrayList<Integer>(8);

		obj.put("reg_str", reg_str);
		obj.put("msg", msg);
		if (result) {
			for (int i = 0; i < 8; i++)
				ai_states.add(ai_state[i]);
			obj.put("analog_input", ai_states);
		}
		obj.put("result", result);
		
		return obj.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	public static String genDeviceListResponse(Boolean result, ResultSet result_set, String msg) {
		final JSONObject obj = new JSONObject();
		final List<JSONObject> device_info = new ArrayList<JSONObject>();
		
		if (result) {
			try {
				while (result_set.next()) {
					JSONObject jobj = new JSONObject();
					String rsg_str = result_set.getString("name");
					jobj.put("name", rsg_str);
					jobj.put("id", result_set.getInt("id"));
					jobj.put("regs", result_set.getString("regs"));
					jobj.put("onl", result_set.getBoolean("online"));
					jobj.put("conn_lt", result_set.getString("connect_last_time"));
					jobj.put("lat", result_set.getDouble("latitude"));
					jobj.put("lng", result_set.getDouble("longitude"));
					jobj.put("desc", result_set.getString("description"));
					device_info.add(jobj);
				}
				obj.put("data", device_info);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				msg = e.getMessage();
				result = false;
			}
		}
		obj.put("msg", msg);
		obj.put("result", result);
		
		return obj.toJSONString();
	}
}
