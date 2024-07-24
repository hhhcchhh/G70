package com.dwdbsdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import com.dwdbsdk.Bean.GnbTimingOffset;
import com.dwdbsdk.FTP.FTPUtil;
import com.dwdbsdk.File.FileUtil;
import com.dwdbsdk.Interface.DBBusinessListener;
import com.dwdbsdk.Interface.FtpListener;
import com.dwdbsdk.Interface.DWBusinessListener;
import com.dwdbsdk.Interface.SocketStateListener;
import com.dwdbsdk.Logcat.LogcatHandler;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.Socket.SocketService;
import com.dwdbsdk.Socket.ZTcpService;

public class DwDbSdk {
    private Context _context;
    private boolean predator;
    private Intent serviceIntent;
    private static DwDbSdk instance;

    public static DwDbSdk build() {
        synchronized (DwDbSdk.class) {
            if (instance == null) {
                instance = new DwDbSdk();
            }
        }
        return instance;
    }
    public DwDbSdk() {
    }

    public void changeLogDir(String directory){
        Log.d("NrSdk", "changeLogDir = " + directory);
        while (directory.startsWith("/")) directory = directory.substring(1);
        while (directory.endsWith("/")) directory = directory.substring(0, directory.length() - 1);
        FileUtil.build().changeLogDir(directory);
    }

    /**
     * SDK初始化入口
     */
    public void init(Context context) {
        _context = context;
		new Thread(new Runnable() {
            @Override
            public void run() {
                SdkLog.zipLogFiles();
                SdkLog.createFile();
                SdkLog.I("LDfSdk init start" + ", V" + getSdkVersionName(_context));
            }
        }).start();

        // 开启程序异常捕获
        LogcatHandler logcatHandler = LogcatHandler.getInstance();
        logcatHandler.init(_context);

        // 启动频点采集服务
        //serviceIntent = new Intent(_context, ArfcnService.class);
        //_context.startService(serviceIntent);

        // 启动业务服务
        Intent serviceIntent = new Intent(_context, SocketService.class);
        _context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        GnbTimingOffset.build().init();
        SdkLog.I("LDfSdk init success");
    }

    final ServiceConnection connection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            SdkLog.I("#####onServiceDisconnected");
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            SdkLog.I("#####onServiceConnected");
        }
    };

    public String getSdkVersionName(Context ctx){
        String versionName = "";
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            versionName = pi.versionName == null ? "null" : pi.versionName;
            versionName += ", SDK V1.6.2";
        } catch (PackageManager.NameNotFoundException e) {
            SdkLog.E("sdk getSdkVersionName err:" + e);
        }
        return versionName;
    }

    /**
     * 添加设备连接状态监听，可多个
     */
    public void addConnectListener(SocketStateListener listener){
        ZTcpService.build().addConnectListener(listener);
    }

    /**
     * 移除设备连接状态监听
     */
    public void removeConnectListener(SocketStateListener listener){
        ZTcpService.build().removeConnectListener(listener);
    }

    /**
     * 设置定位业务监听
     */
    public void setDWBusinessListener(DWBusinessListener listener){
        MessageController.build().setDWBusinessListener(listener);
    }

    /**
     * 移除定位业务监听
     */
    public void removeDWBusinessListener(){
        MessageController.build().removeDWBusinessListener();
    }

    /**
     * 设置单兵/干扰业务监听
     *
     * @param predator true 单兵   false 干扰
     */
    public void setDBBusinessListener(boolean predator, DBBusinessListener listener){
        this.predator = predator;
        MessageController.build().setDBBusinessListener(listener);
    }

    /**
     * 移除单兵业务监听
     */
    public void removeDBBusinessListener(){
        MessageController.build().removeDBBusinessListener();
    }

    /**
     * 设置 ftp 业务监听
     */
    public void setFtpListener(FtpListener listener){
        FTPUtil.build().setFtpListener(listener);
    }

    /**
     * 移除 ftp 业务监听
     */
    public void removeFtpListener(){
        FTPUtil.build().removeFtpListener();
    }

    /**
     * 释放 SDK 资源
     */
    public void release() {
        SdkLog.I("LDfSdk release start");

        _context.unbindService(connection);
        MessageController.build().close();
        SdkLog.I("LDfSdk release finish");
        new Thread(new Runnable() {
            @Override
            public void run() {
                SdkLog.zipLogFiles();
            }
        }).start();

    }

    public Context getContext() {
        return _context;
    }
    public boolean isPredator() {
        return predator;
    }
}
