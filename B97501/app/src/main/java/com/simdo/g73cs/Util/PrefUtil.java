package com.simdo.g73cs.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.Logcat.SLog;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Socket.Bean.TracePara;
import com.simdo.g73cs.Bean.ArfcnBean;
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
		int n_tac = tac + GnbProtocol.MAX_TAC_NUM + 1;
		setTac(n_tac);
		return tac;
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

	public void putFreqArfcnList(String band, List<ArfcnBean> list){
		SharedPreferences.Editor editor = APP_PREF.edit();
		Gson gson = new Gson();
		String json = gson.toJson(list);
		editor.putString(band, json);
		editor.apply();
	}

	public List<ArfcnBean> getFreqArfcnList(String band){
		String json = APP_PREF.getString(band,"");
		Gson gson = new Gson();
		Type type = new TypeToken<ArrayList<ArfcnBean>>(){}.getType();
		return gson.fromJson(json, type);
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

	private final Context mContext;
	private static PrefUtil instance;
	private SharedPreferences APP_PREF;
}