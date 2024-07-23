package com.dwdbsdk;

import android.content.Context;
import android.content.Intent;

import com.dwdbsdk.Arfcn.Nr5g;
import com.dwdbsdk.Bean.GnbTimingOffset;
import com.dwdbsdk.FTP.FTPUtil;
import com.dwdbsdk.Interface.DBBusinessListener;
import com.dwdbsdk.Interface.FtpListener;
import com.dwdbsdk.Interface.DWBusinessListener;
import com.dwdbsdk.Interface.Nr5gScanArfcnListener;
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

    /**
     * SDK初始化入口
     */
    public void init(Context context) {
        SdkLog.I("LDfSdk init start");
        _context = context;
		new Thread(new Runnable() {
            @Override
            public void run() {
                SdkLog.zipLogFiles();
                SdkLog.createFile();
            }
        }).start();

        // 开启程序异常捕获
        LogcatHandler logcatHandler = LogcatHandler.getInstance();
        logcatHandler.init(_context);

        // 启动频点采集服务
        //serviceIntent = new Intent(_context, ArfcnService.class);
        //_context.startService(serviceIntent);

        // 启动业务服务
        serviceIntent = new Intent(_context, SocketService.class);
        _context.startService(serviceIntent);

        GnbTimingOffset.build().init();
        SdkLog.I("LDfSdk init success");

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
     * 设置 sdk(非基带) 扫频 业务监听
     */
    public void addNr5gScanArfcnListener(Nr5gScanArfcnListener listener){
        Nr5g.build().addOnScanArfcnListener(listener);
    }

    /**
     * 移除sdk(非基带) 扫频 业务监听
     */
    public void removeNr5gScanArfcnListener(Nr5gScanArfcnListener listener) {
        Nr5g.build().removeNr5gScanArfcnListener(listener);
    }

    /**
     * 释放 SDK 资源
     */
    public void release() {
        SdkLog.I("LDfSdk release start");

        _context.stopService(serviceIntent);
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
