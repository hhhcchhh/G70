package com.nr;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.Logcat.LogcatHandler;
import com.Logcat.SLog;
import com.nr.Gnb.Bean.GnbTimingOffset;
import com.nr.Socket.UdpControl;

public class NrSdk {
    private Context _context;
    private static NrSdk instance;

    public static NrSdk build() {
        synchronized (NrSdk.class) {
            if (instance == null) {
                instance = new NrSdk();
            }
        }
        return instance;
    }
    public NrSdk() {

    }

    /**
     * SDK初始化入口
     *
     * @param context
     */
    public void init(Context context) {
        _context = context;
		new Thread(new Runnable() {
            @Override
            public void run() {
                SLog.zipLogFiles();
                SLog.createFile();
                SLog.I("#### NrSdk$init()" + ", V" + getSdkVersionName(_context));
            }
        }).start();
        LogcatHandler logcatHandler = LogcatHandler.getInstance();
        logcatHandler.init(_context);

        UdpControl.build().startUdp();
        GnbTimingOffset.build().init();
    }

    public void onDestory() {
        SLog.I("#### NrSdk$onDestory()");

        UdpControl.build().closeSocket();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SLog.zipLogFiles();
            }
        }).start();

    }

    public String getSdkVersionName(Context ctx){
        String versionName = "";
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            versionName = pi.versionName == null ? "null" : pi.versionName;
            versionName += ", SDK V1.6.3";
        } catch (PackageManager.NameNotFoundException e) {
            SLog.E("sdk getSdkVersionName err:" + e);
        }
        return versionName;
    }

    public Context getContext() {
        return _context;
    }
}
