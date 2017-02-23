package com.mycompany.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
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
	public static String genGenericDeviceResponse(Boolean result, String msg, int code) {
		
		final JSONObject obj = new JSONObject();
		
		obj.put("msg", msg);
		obj.put("result", result);
		obj.put("code", code);
		
		return obj.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	public static String genGenericUserResponse(Boolean result, String msg, int code) {
		
		final JSONObject obj = new JSONObject();

		obj.put("msg", msg);
		obj.put("result", result);
		obj.put("code", code);
		
		return obj.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	public static String genUserLoginResponse(Boolean result, String msg, String session_id, int permission, int code) {
		
		final JSONObject obj = new JSONObject();
		
		obj.put("msg", msg);
		obj.put("result", result);
		if (result) {
			obj.put("session_id", session_id);
			obj.put("permision", permission);
		}
		
		return obj.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	public static String genDInResponse(Boolean result, byte di_state, String msg,int code) {
		
		final JSONObject obj = new JSONObject();
		final List<Integer> di_states = new ArrayList<Integer>(8);

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
		obj.put("code", code);
		
		return obj.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	public static String genAInResponse(Boolean result, int[] ai_state, String msg, int code) {
		final JSONObject obj = new JSONObject();
		final List<Integer> ai_states = new ArrayList<Integer>(8);

		obj.put("msg", msg);
		if (result) {
			for (int i = 0; i < 8; i++)
				ai_states.add(ai_state[i]);
			obj.put("analog_input", ai_states);
		}
		obj.put("result", result);
		obj.put("code", code);
		
		return obj.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	public static String genDeviceListResponse(Boolean result, JSONArray device_info, String msg, int code) {
		final JSONObject obj = new JSONObject();
		
		if (result) {
			obj.put("data", device_info);
		}
		obj.put("msg", msg);
		obj.put("result", result);
		obj.put("code", code);
		
		return obj.toJSONString();
	}
}
