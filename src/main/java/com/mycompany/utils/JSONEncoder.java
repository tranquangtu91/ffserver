package com.mycompany.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

public class JSONEncoder {
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
}
