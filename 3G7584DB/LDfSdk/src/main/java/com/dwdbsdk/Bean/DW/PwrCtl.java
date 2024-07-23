package com.dwdbsdk.Bean.DW;

import com.dwdbsdk.Arfcn.Nr5g;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.Native.Native;

import java.util.Timer;
import java.util.TimerTask;

public class PwrCtl {
    private static final int DELAY_TIMER = 2500;

    private static PwrCtl instance;

    public static PwrCtl build() {
        synchronized (PwrCtl.class) {
            if (instance == null) {
                instance = new PwrCtl();
            }
        }
        return instance;
    }

    public PwrCtl() {
    }

    /**
     * 移动4G模块电源控制
     *
     * @param enable
     */
    public void Lte(boolean enable) {
        SdkLog.I("Lte() enable = " + enable);
        if (enable) {
            Native.SetModulePwr(AtCmd.PWR.LTE, 1);
            Timer mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Native.SetModulePwr(AtCmd.PWR.LTE, 0);
                }
            }, DELAY_TIMER);
        } else {
            //SerialPortController.build().sendAtCmd(Nr5g.sAT_CPOF);
        }
    }

    /**
     * 移动5G模块电源控制
     *
     * @param enable
     */
    public void Nr5g(boolean enable) {
        SdkLog.I("Nr5g() enable = " + enable);
        if (enable) {
            SdkLog.I("Nr5g() pwr = 1");
            Native.SetModulePwr(AtCmd.PWR.NR, 1);
            Timer mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    SdkLog.I("Nr5g() pwr = 0");
                    Native.SetModulePwr(AtCmd.PWR.NR, 0);
                    Nr5g.build().startTimeDelay();
                }
            }, DELAY_TIMER);
        } else {
            //SerialPortController.build().sendAtCmd(Nr5g.sAT_CPOF);
        }
    }

    /**
     * 模块电源控制
     */
    public void modulePwrCtl(boolean enable) {
        Nr5g(enable);
    }
}
