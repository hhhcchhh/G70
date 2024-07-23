package com.dwdbsdk.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.dwdbsdk.Bean.DW.TracePara;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.Logcat.SdkLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
        mContext = DwDbSdk.build().getContext();
        initPreferences();
    }

    @SuppressWarnings("deprecation")
    private void initPreferences() {
        mSharedPreferences = mContext.getSharedPreferences(SDK_PREF, Context.MODE_PRIVATE);
    }

    public void setLastWorkInfo(String id, List<TracePara> traceList) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if (id.isEmpty() || traceList == null) {
            editor.remove("LAST_WORK_INFO");
            return;
        }
        PackageManager packageManager = mContext.getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            String packageName = packageInfo.packageName;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(packageName + "#" + id, traceList);
            editor.putString("LAST_WORK_INFO", jsonObject.toString());
            editor.apply();
            SdkLog.D("setLastWorkInfo(): packageName = " + packageName + ", deviceId = " + id + ", traceList = " + traceList);
        } catch (Exception e) {
            SdkLog.D("setLastWorkInfo(): e = " + e);
        }
    }

    public List<TracePara> getLastWorkInfo(String id) {
        String jsonStr = mSharedPreferences.getString("LAST_WORK_INFO", "");
        SdkLog.D("getLastWorkInfo(): jsonStr = " + jsonStr);
        if (jsonStr.isEmpty()) return null;

        List<TracePara> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.length() > 0) {
                PackageManager packageManager = mContext.getPackageManager();
                PackageInfo packageInfo = null;
                try {
                    packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);

                    String packageName = packageInfo.packageName;
                    String info = jsonObject.getString(packageName + "#" + id).replaceAll("TracePara", "");
                    JSONArray jsonArray = new JSONArray(info);

                    for (int i = 0; i < jsonArray.length(); i++) {
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
                                object.getString("splitArfcndl")));
                    }
                } catch (Exception e) {
                    SdkLog.D("getLastWorkInfo(): e = " + e);
                }
            }

        } catch (JSONException e) {
            SdkLog.D("getLastWorkInfo(): e = " + e);
        }

        return list;
    }

    public void setSsbList(String list) {
        SdkLog.D("SSB_LIST: " + list);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("SSB_LIST", list);
        editor.apply();
    }

    public String getSsbList() {
        return mSharedPreferences.getString("SSB_LIST", "");
    }

    public void setArfcnMode(boolean mode) {
        SdkLog.D("setArfcnMode(): mode = " + mode);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(ARFCN_MODE, mode);
        editor.apply();
    }

    public boolean getArfcnMode() {
        boolean mode = mSharedPreferences.getBoolean(ARFCN_MODE, false);
        SdkLog.D("getArfcnMode(): mode = " + mode);
        return mode;
    }

    public void setTimingOffset(String list) {
        //SdkLog.D("setTimingOffset(): list = " + list);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(TIMING_OFFSET, list);
        editor.apply();
    }

    public String getTimingOffset() {
        //SdkLog.D("getTimingOffset(): = " + list);
        return mSharedPreferences.getString(TIMING_OFFSET, "");
    }

    private Context mContext;
    private static SdkPref instance;
    private SharedPreferences mSharedPreferences;
    private final String SDK_PREF = "sdk_pref";
    private final String TIMING_OFFSET = "gnb_timing_offset";
    private final String ARFCN_MODE = "arfcn_mode";
    private final String LAST_WORK_INFO = "last_work_info";
}