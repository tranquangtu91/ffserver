package com.mycompany.ffserver;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mycompany.database.DbUtils;

public class FFDevInfo {
	int size = 50;
	
    List<String> reg_str_lst;
    List<Boolean> in_use_flags;
    List<Boolean> online_flags;
    List<Boolean> old_online_flags;

    public FFDevInfo(int size) {
    	this.size = size;
        this.in_use_flags = new ArrayList<Boolean>(size);
        this.reg_str_lst = new ArrayList<String>(size);
        this.online_flags = new ArrayList<Boolean>(size);
        this.old_online_flags = new ArrayList<Boolean>(size);
    }
    
    public void addRegStr(String reg_str) {
        if (!reg_str_lst.contains(reg_str)) {
            reg_str_lst.add(reg_str);
            in_use_flags.add(false);
            online_flags.add(false);
            old_online_flags.add(false);
        }
    }
    
    public void removeRegStr(String reg_str) {
        int id = reg_str_lst.indexOf(reg_str);
        if (id != -1) {
            reg_str_lst.remove(id);
            in_use_flags.remove(id);
            online_flags.remove(id);
            old_online_flags.remove(id);
        }
    }
    
    public void lockRegStr(String reg_str) {
        int id = reg_str_lst.indexOf(reg_str);
        if (id != -1) {
            in_use_flags.set(id, true);
            online_flags.set(id, true);
            if (old_online_flags.get(id) != true) {
            	try {
					DbUtils.updateOnlineState(reg_str, true);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	old_online_flags.set(id, true);
            }
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
    		if (!in_use_flags.get(i)) {
    			online_flags.set(i, false);
    			if (old_online_flags.get(i)) {
    				try {
						DbUtils.updateOnlineState(reg_str_lst.get(i), false);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				old_online_flags.set(i, false);
    			}
    		}
    		in_use_flags.set(i, false);
    	}
    }
    
    public Boolean isOnline(String reg_str) {
    	int id = reg_str_lst.indexOf(reg_str);
    	if (id != -1) {
    		return online_flags.get(id);
    	}
    	return false;
    }
}
