package com.dwdbsdk.Arfcn;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.dwdbsdk.Bean.DW.PwrCtl;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.Native.Native;
import com.dwdbsdk.SerialPort.SerialPortController;

/**
 * 此频点采集服务为SDK采集，需要板子有对应的芯片，非基带板采集
 */
public class ArfcnService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
		SdkLog.I("#### ArfcnService: onCreate()");
        initNative();
    }

    private void initNative() {
        boolean res = Native.LcInit();
        if (res) {
            SdkLog.E( "#### ArfcnService(): Device ready!");
        } else {
            SdkLog.E( "#### ArfcnService(): Device no ready!");
        }
        // 配置USB接口可用,SIM卡不连接5G模块
        Native.EnableNrUsb(0);
        Native.EnableHOST(0);
        Native.EnableOTG(0);
        Native.SetCardTo5G(0);
        Native.EnableTypec(1);

        PwrCtl.build().modulePwrCtl(true);
        SerialPortController.build();
        Nr5g.build().init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SdkLog.I("#### ArfcnService: onDestroy()");
        Nr5g.build().stop();
        SerialPortController.build().onStop();
        Native.LcClose();
    }
}
