package com.dwdbsdk.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.dwdbsdk.Bean.DW.TracePara;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.Logcat.SdkLog;

import org.json.JSONObject;

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