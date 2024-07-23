package com.simdo.g73cs.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.dwdbsdk.Bean.DW.DWProtocol;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.ZApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
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
		mPreWifi = mContext.getSharedPreferences(WIFI_CFG, Context.MODE_PRIVATE);
		mPreBtName = mContext.getSharedPreferences(BT_NAME, Context.MODE_PRIVATE);
		mPreDevName = mContext.getSharedPreferences(DEV_NAME, Context.MODE_PRIVATE);

		mPreBtMac = mContext.getSharedPreferences(BT_MAC, Context.MODE_PRIVATE);
		mPreCityList = mContext.getSharedPreferences(PRE_CITY_LIST, Context.MODE_PRIVATE);
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
		editor.commit();
	}

	public int getTac() {
		int tac = APP_PREF.getInt("cfg_tac", 1234);
		int n_tac = tac + DWProtocol.MAX_TAC_NUM + 1;
		setTac(n_tac);
		return tac;
	}

	public void setDropImsiList(List<String> imsiList) {
		StringBuilder imsi = new StringBuilder();
		for (int i = 0; i < imsiList.size(); i++) {
			imsi.append(imsiList.get(i));
			imsi.append(";");
		}
		SharedPreferences.Editor editor = APP_PREF.edit();
		editor.putString("drop_imsi", imsi.toString());
		editor.apply();
	}

	public List<String> getDropImsiList() {
		String imsi = APP_PREF.getString("drop_imsi", null);

		List<String> imsiList = new ArrayList<String>();
		if (imsi != null && imsi.length() > 0) {
			String[] data = imsi.split(";");
			for (String datum : data) {
				if (!datum.equals("")) {
					imsiList.add(datum);
				}
			}
		}
		return imsiList;
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
	public void configWifi(String key,String wifi) {
		SharedPreferences.Editor editor = mPreWifi.edit();
		editor.putString(key, wifi);
		editor.commit();
	}

	public String getWifiInfo(String key) {
		String wifi = mPreWifi.getString(key, "");
		return wifi;
	}

	public void setDBBtName(String bt_name) {
		SharedPreferences.Editor editor = mPreBtName.edit();
		editor.putString(BT_NAME, bt_name);
		editor.commit();
	}

	public String getDBBtName() {
		String bt_name = mPreBtName.getString(BT_NAME, "G25");
		return bt_name;
	}
	public void setDevName(String key,String dev_name) {
		SharedPreferences.Editor editor = mPreDevName.edit();
		editor.putString(key, dev_name);
		editor.apply();
	}

	public String getDevName(String key) {
		return mPreDevName.getString(key, "待连接..");
	}

	public void removeKey(String key){
		SharedPreferences.Editor editor = mPreDevName.edit();
		editor.remove(key);
		editor.apply();
	}

	public void setBtMac(String mac) {
		SharedPreferences.Editor editor = mPreBtMac.edit();
		editor.putString(BT_MAC, mac);
		editor.commit();
	}

	public String getBtMac() {
		String mac = mPreBtMac.getString(BT_MAC, "");
		return mac;
	}

	public void setCityList(String list) {
		AppLog.D("setCityList(): list = " + list);
		SharedPreferences.Editor editor = mPreCityList.edit();
		editor.putString(PRE_CITY_LIST, list);
		editor.commit();
	}

	public String getCityList() {
		String list = mPreCityList.getString(PRE_CITY_LIST, "");
		AppLog.D("getArfcnList(): list = " + list);

		return list;
	}
	public int getInt(String key) {
		return mPreDevName.getInt(key, -1);
	}
	public String getDevNameString(String key) {
		return mPreDevName.getString(key, "");
	}
	public void setDevNameString(String key, String value) {
		SharedPreferences.Editor editor = mPreDevName.edit();
		editor.putString(key, value);
		editor.apply();
	}
	private final Context mContext;
	private static PrefUtil instance;
	private SharedPreferences APP_PREF;

	private SharedPreferences mPreBtName;
	private SharedPreferences mPreDevName;
	private SharedPreferences mPreBtMac;
	private SharedPreferences mPreWifi;
	private SharedPreferences mPreCityList;
	private final String WIFI_CFG = "wifi_cfg";
	private final String BT_NAME = "bt_name";
	private final String DEV_NAME = "dev_name";
	private final String BT_MAC = "bt_mac";
	private final String PRE_CITY_LIST = "city_list";
	public final String rx_gain_key = "rx_gain_key";
}