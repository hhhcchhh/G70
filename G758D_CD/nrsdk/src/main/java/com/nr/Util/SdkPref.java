package com.nr.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.Logcat.SLog;
import com.nr.NrSdk;
import com.nr.Socket.Bean.TracePara;
import com.nr.Socket.MessageControl.MessageHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("WorldReadableFiles")
public class SdkPref {
	public static SdkPref build() {
        synchronized (SdkPref.class) {
            if (instance == null) {
                instance = new SdkPref();
            }
        }
        return instance;
    }

	public SdkPref() {
		super();
		mContext = NrSdk.build().getContext();
		initPreferences();
	}
	
	@SuppressWarnings("deprecation")
	private void initPreferences() {
		mSharedPreferences = mContext.getSharedPreferences(SDK_PREF, Context.MODE_PRIVATE);
	}

	public void setLastWorkInfo(String id, boolean isLte, List<TracePara> traceList){
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		PackageManager packageManager = mContext.getPackageManager();

		List<TracePara> add_list = new ArrayList<>();
		if (traceList!=null){
			for (TracePara para : traceList) {
				if (isLte){
					if (para.isLte()) add_list.add(para);
				}else {
					if (!para.isLte()) add_list.add(para);
				}
			}
		}

		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
			String packageName = packageInfo.packageName;
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(packageName + "#" + id, add_list);
			editor.putString(isLte ? "NR_LAST_WORK_INFO" : "LTE_LAST_WORK_INFO", jsonObject.toString());
			editor.apply();
			SLog.D("setLastWorkInfo(): packageName = " + packageName + ", deviceId = " + id + ", traceList = " + add_list);
		} catch (Exception e) {
			SLog.D("setLastWorkInfo(): e = " + e);
		}
	}

	public List<TracePara> getLastWorkInfo(String id, boolean isLte){
		String jsonStr = mSharedPreferences.getString(isLte ? "NR_LAST_WORK_INFO" : "LTE_LAST_WORK_INFO", "");
		SLog.D("getLastWorkInfo(): jsonStr = " + jsonStr);
		if (jsonStr.isEmpty()) return null;

		List<TracePara> list = new ArrayList<>();
		try {
			JSONObject jsonObject = new JSONObject(jsonStr);
			if (jsonObject.length() > 0){
				PackageManager packageManager = mContext.getPackageManager();
				PackageInfo packageInfo = null;
				try {
					packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);

					String packageName = packageInfo.packageName;
					String info = jsonObject.getString(packageName + "#" + id).replaceAll("TracePara", "");
					JSONArray jsonArray = new JSONArray(info);

					for (int i = 0; i < jsonArray.length(); i++){
						JSONObject object = new JSONObject(jsonArray.get(i).toString());
						list.add(new TracePara(
								object.getString("id"),
								object.getBoolean("isLte"),
								object.getInt("cellId"),
								object.getString("imsi"),
								object.getString("plmn"),
								object.getString("arfcn"),
								object.getString("pci"),
								object.getString("ueMaxTxpwr"),
								object.getInt("startTac"),
								object.getInt("maxTac"),
								object.getInt("timingOffset"),
								object.getInt("workMode"),
								object.getInt("airSync"),
								object.getString("plmn1"),
								object.getInt("ulRbOffset"),
								object.getLong("cid"),
								object.getInt("ssbBitmap"),
								object.getInt("bandWidth"),
								object.getInt("cfr"),
								object.getInt("swapRf"),
								object.getInt("rejectCode"),
								object.getInt("rxLevMin"),
								object.getInt("redirect2LteArfcn"),
								object.getInt("mobRejectCode"),
								object.getString("splitArfcndl"),
								object.getInt("forceCfg")));
					}
				} catch (Exception e) {
					SLog.D("getLastWorkInfo(): e = " + e);
				}
			}

		} catch (JSONException e) {
			SLog.D("getLastWorkInfo(): e = " + e);
		}

		return list;
	}

	public void setArfcnMode(boolean mode) {
		SLog.D("setArfcnMode(): mode = " + mode);
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putBoolean(ARFCN_MODE, mode);
		editor.apply();
	}

	public boolean getArfcnMode() {
		boolean mode = mSharedPreferences.getBoolean(ARFCN_MODE, false);
		SLog.D("getArfcnMode(): mode = " + mode);
		return mode;
	}

	public void setTimingOffset(String list) {
		//SLog.D("setTimingOffset(): list = " + list);
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putString(TIMING_OFFSET, list);
		editor.commit();
	}

	public String getTimingOffset() {
		String list = mSharedPreferences.getString(TIMING_OFFSET, "");
		//SLog.D("getTimingOffset(): = " + list);

		return list;
	}
	private Context mContext;
	private static SdkPref instance;
	private SharedPreferences mSharedPreferences;
	private final String SDK_PREF = "sdk_pref";
	private final String TIMING_OFFSET = "gnb_timing_offset";
	private final String ARFCN_MODE = "arfcn_mode";
	private final String LAST_WORK_INFO = "last_work_info";
}