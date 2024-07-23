package com.g50.UI.Bean;

import com.nr70.Gnb.Response.GnbStateRsp;

public class DeviceInfo {
    private static DeviceInfo instance;

    public static DeviceInfo build() {
        synchronized (DeviceInfo.class) {
            if (instance == null) {
                instance = new DeviceInfo();
            }
        }
        return instance;
    }

    public DeviceInfo() {
        ssid = "";
        btName = "";
        deviceId = "";
        softVer = "";
        fpgaVer = "";
        hwVer = "";
        devName = "unKnown";
        license = "";
        wifiIp = "0.0.0.0";
        gpsEnable = GnbStateRsp.Gps.FAIL;
    }

    public static DeviceInfo getInstance() {
        return instance;
    }

    public static void setInstance(DeviceInfo instance) {
        DeviceInfo.instance = instance;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBtName() {
        return btName;
    }

    public void setBtName(String btName) {
        this.btName = btName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSoftVer() {
        return softVer;
    }

    public void setSoftVer(String softVer) {
        this.softVer = softVer;
    }

    public String getFpgaVer() {
        return fpgaVer;
    }

    public void setFpgaVer(String fpgaVer) {
        this.fpgaVer = fpgaVer;
    }

    public String getHwVer() {
        return hwVer;
    }

    public void setHwVer(String hwVer) {
        this.hwVer = hwVer;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getWifiIp() {
        return wifiIp;
    }

    public void setWifiIp(String wifiIp) {
        this.wifiIp = wifiIp;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public int getGpsEnable() {
        return gpsEnable;
    }

    public void setGpsEnable(int gpsEnable) {
        this.gpsEnable = gpsEnable;
    }

    private String ssid, devName, license, wifiIp, btName, deviceId, softVer, fpgaVer, hwVer;
    private int gpsEnable;
}
