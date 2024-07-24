package com.simdo.dw_db_s.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.simdo.dw_db_s.Bean.MyUeidBean;
import com.simdo.dw_db_s.ZApplication;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressLint("WorldReadableFiles")
public class PrefUtil {
	public static PrefUtil build() {
        synchronized (PrefUtil.class) {
            if (instance == null) {
                instance = new PrefUtil();
            }
        }
        return instance;
    }

	public PrefUtil() {
		super();
		mContext = ZApplication.getInstance().getContext();
		initPreferences();
	}
	public void initPreferences() {
		APP_PREF = mContext.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE);
	}

	public void putValue(String key, Object value) {
		SharedPreferences.Editor editor = APP_PREF.edit();
		if (value instanceof String) {
			editor.putString(key, (String) value);
		} else if (value instanceof Boolean) {
			editor.putBoolean(key, (Boolean) value);
		} else if (value instanceof Float) {
			editor.putFloat(key, (Float) value);
		} else if (value instanceof Integer) {
			editor.putInt(key, (Integer) value);
		} else  if (value instanceof Long) {
			editor.putLong(key, (Long) value);
		} else if (value instanceof Set) {
			Set set = (Set) value;
			Set<String> newSet = new HashSet<>();
			for (Object object : set) {
				newSet.add((String)object);
			}
			editor.putStringSet(key, newSet);
		} else {
			throw new IllegalArgumentException("SharedPreferences 不支持存储此类型的值");
		}

		editor.apply();
	}

	public Object getValue(String key, Object defaultValue) {
		if (defaultValue instanceof String) {
			return APP_PREF.getString(key, (String) defaultValue);
		} else if (defaultValue instanceof Boolean) {
			return APP_PREF.getBoolean(key, (Boolean) defaultValue);
		} else if (defaultValue instanceof Float) {
			return APP_PREF.getFloat(key, (Float) defaultValue);
		} else if (defaultValue instanceof Integer) {
			return APP_PREF.getInt(key, (Integer) defaultValue);
		} else if (defaultValue instanceof Long) {
			return APP_PREF.getLong(key, (Long) defaultValue);
		} else if (defaultValue instanceof Set) {
			return APP_PREF.getStringSet(key, (Set<String>) defaultValue);
		} else {
			return null;
		}
	}

	public void setTac(int tac) {
		SharedPreferences.Editor editor = APP_PREF.edit();
		if (tac > 3000000) {
			tac = 1234;
		}
		editor.putInt("cfg_tac", tac);
		editor.apply();
	}

	public int getTac() {
		int tac = APP_PREF.getInt("cfg_tac", 1234);
		int n_tac = tac + 6 + 1;
		setTac(n_tac);
		return tac;
	}
	public void setBtName(String bt_name) {
		SharedPreferences.Editor editor = APP_PREF.edit();
		editor.putString("bt_name", bt_name);
		editor.apply();
	}

	public String getBtName() {
		String bt_name = APP_PREF.getString("bt_name", "G25");
		return bt_name;
	}

	public void configWifi(String wifi) {
		SharedPreferences.Editor editor = APP_PREF.edit();
		editor.putString("wifi_cfg", wifi);
		editor.apply();
	}

	public String getWifiInfo() {
		String wifi = APP_PREF.getString("wifi_cfg", "");
		return wifi;
	}

	public void putUeidList(List<MyUeidBean> list){
		SharedPreferences.Editor editor = APP_PREF.edit();
		Gson gson = new Gson();
		String json = gson.toJson(list);
		editor.putString("ueid_list", json);
		editor.apply();
	}

	public List<MyUeidBean> getUeidList(){
		String json = APP_PREF.getString("ueid_list","");
		Gson gson = new Gson();
		Type type = new TypeToken<ArrayList<MyUeidBean>>(){}.getType();
		return gson.fromJson(json, type);
	}
	public void setBlackList(List<String> blackList){
		StringBuffer imsi = new StringBuffer();
		SharedPreferences.Editor editor = APP_PREF.edit();
		for (int i = 0; i < blackList.size(); i++) {
			if (blackList.get(i).length() == 15){
				imsi.append(blackList.get(i));
				imsi.append(";");
			}
		}
		editor.putString("black_list", imsi.toString());
		editor.apply();
	}
	public List<String> getBlackList(){
		String imsi = APP_PREF.getString("black_list","4600000000000000");
		List<String> blackList = new ArrayList<>();
		String[] list = imsi.split(";");
		for (int i = 0; i < list.length; i++) {
			if (list[i].length() == 15){
				blackList.add(list[i]);
			}
		}
		return blackList;
	}

	public String getString(String key) {
		return APP_PREF.getString(key, "");
	}

	public void setString(String key, String value){
		SharedPreferences.Editor editor = APP_PREF.edit();
		editor.putString(key, value);
		editor.apply();
	}
	private final Context mContext;
	private static PrefUtil instance;
	private SharedPreferences APP_PREF;


}