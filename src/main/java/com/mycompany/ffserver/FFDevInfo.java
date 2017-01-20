package com.mycompany.ffserver;

import java.util.ArrayList;
import java.util.List;

public class FFDevInfo {
	int size = 50;
	
    List<String> reg_str_lst;
    List<Boolean> in_use_flags;
    List<Boolean> is_online;

    public FFDevInfo(int size) {
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
