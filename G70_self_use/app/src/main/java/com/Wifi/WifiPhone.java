package com.Wifi;

import android.net.wifi.ScanResult;
import com.g50.ZApplication;
import java.util.ArrayList;
import java.util.List;

public class WifiPhone {
    private static WifiPhone instance;
    public static WifiPhone build() {
        synchronized (WifiPhone.class) {
            if (instance == null) {
                instance = new WifiPhone();
            }
        }
        return instance;
    }

    public WifiPhone() {
    	mCurList.clear();
        mWifiAdmin = new WifiAdmin(ZApplication.context());
        getScanResult();
    }

    public List<ScanResult> getScanResult() {
        mWifiAdmin.openWifi();
        mWifiAdmin.startScan();
        mWifiScanResult = mWifiAdmin.getWifiList();
        return mWifiScanResult;
    }

    public void onWifiResume() {
        if (mWifiAdmin != null) {
            mWifiAdmin.onResume();
        }
    }

    public void onWifiDestory() {
        if (mWifiAdmin != null) {
            mWifiAdmin.onDestory();
        }
    }

    public List<WifiBean> getWifiInfo() {
        mCurList.clear();
        List<ScanResult> wifiinfo = getScanResult();
        if (wifiinfo != null) {
            for (int i = 0; i < wifiinfo.size(); i++) {
                mCurList.add(new WifiBean(wifiinfo.get(i).SSID, wifiinfo.get(i).BSSID,
                        wifiinfo.get(i).level, wifiinfo.get(i).frequency, wifiinfo.get(i).capabilities));
            }
        }
        return mCurList;
    }
    public List<WifiBean> getList() {
        return mCurList;
    }

    public String getFirstData() {
        //if (mCurList.size() > 0) {
            mFirstData = "WIFI: 检测到" + mCurList.size() + "个热点数据";
        //}
        return mFirstData;
    }


    public int calcChanel(int freq) {
        if (freq == 2412) {
            return 1;
        } else if (freq == 2417) {
            return 2;
        } else if (freq == 2422) {
            return 3;
        } else if (freq == 2427) {
            return 4;
        } else if (freq == 2432) {
            return 5;
        } else if (freq == 2437) {
            return 6;
        } else if (freq == 2442) {
            return 7;
        } else if (freq == 2447) {
            return 8;
        } else if (freq == 2452) {
            return 9;
        } else if (freq == 2457) {
            return 10;
        } else if (freq == 2462) {
            return 11;
        } else if (freq == 2467) {
            return 12;
        } else if (freq == 2472) {
            return 13;
        } else if (freq == 2484) {
            return 14;
        } else if (freq == 5180) {
            return 36;
        } else if (freq == 5190) {
            return 38;
        } else if (freq == 5200) {
            return 40;
        } else if (freq == 5210) {
            return 42;
        } else if (freq == 5220) {
            return 44;
        } else if (freq == 5230) {
            return 46;
        } else if (freq == 5240) {
            return 48;
        } else if (freq == 5260) {
            return 52;
        } else if (freq == 5270) {
            return 54;
        } else if (freq == 5280) {
            return 56;
        } else if (freq == 5290) {
            return 58;
        } else if (freq == 5300) {
            return 60;
        } else if (freq == 5310) {
            return 62;
        } else if (freq == 5320) {
            return 64;
        } else if (freq == 5500) {
            return 100;
        } else if (freq == 5510) {
            return 102;
        } else if (freq == 5520) {
            return 104;
        } else if (freq == 5530) {
            return 106;
        } else if (freq == 5540) {
            return 108;
        } else if (freq == 5550) {
            return 110;
        } else if (freq == 5560) {
            return 112;
        } else if (freq == 5580) {
            return 116;
        } else if (freq == 5590) {
            return 118;
        } else if (freq == 5600) {
            return 120;
        } else if (freq == 5610) {
            return 122;
        } else if (freq == 5620) {
            return 124;
        } else if (freq == 5630) {
            return 126;
        } else if (freq == 5640) {
            return 128;
        } else if (freq == 5660) {
            return 132;
        } else if (freq == 5670) {
            return 134;
        } else if (freq == 5680) {
            return 136;
        } else if (freq == 5690) {
            return 138;
        } else if (freq == 5700) {
            return 140;
        } else if (freq == 5710) {
            return 142;
        } else if (freq == 5720) {
            return 144;
        } else if (freq == 5745) {
            return 149;
        } else if (freq == 5755) {
            return 151;
        } else if (freq == 5765) {
            return 153;
        } else if (freq == 5775) {
            return 155;
        } else if (freq == 5785) {
            return 157;
        } else if (freq == 5795) {
            return 159;
        } else if (freq == 5805) {
            return 161;
        } else if (freq == 5825) {
            return 165;
        } else if (freq == 5835) {
            return 167;
        } else if (freq == 5845) {
            return 169;
        } else if (freq == 5855) {
            return 171;
        } else if (freq == 5865) {
            return 173;
        } else if (freq == 5875) {
            return 175;
        } else if (freq == 5885) {
            return 177;
        } else {
            return 0;
        }
    }
    
    private String mFirstData = "未检测到WIFI数据";
    private List<WifiBean> mCurList = new ArrayList<WifiBean>();
    private WifiAdmin mWifiAdmin;
    private List<ScanResult> mWifiScanResult;
}
