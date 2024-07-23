package com.simdo.g758s_predator.Bean;


import com.dwdbsdk.Util.BatteryPredator;

public class DbDeviceInfoBean {
    String name = "待连接..";
    BatteryPredator batteryUtil = new BatteryPredator();
    String temp = "0";
    int location = 0;
    int gpsState = 0;
    int airSyncState = 2;
    int autoCfgState = 0;
    int pci = 0;
    String wifiIp = "";
    int lastMsgSn = 0;
    int workState = GnbBean.DB_State.NONE;

    public String getStateStr() {
        return stateStr;
    }

    public void setStateStr(String stateStr) {
        this.stateStr = stateStr;
    }

    String stateStr = "";
    boolean isUpdate = false;//是否更新过
    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }
    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }
    public int getLastMsgSn() {
        return lastMsgSn;
    }
    public void setLastMsgSn(int lastMsgSn) {
        this.lastMsgSn = lastMsgSn;
    }
    public DbDeviceInfoBean() {
    }

    public int getWorkState() {
        return workState;
    }

    public void setWorkState(int workState) {
        this.workState = workState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BatteryPredator getBatteryUtil() {
        return batteryUtil;
    }

    public void setBatteryUtil(BatteryPredator batteryUtil) {
        this.batteryUtil = batteryUtil;
    }
    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public int getGpsState() {
        return gpsState;
    }

    public void setGpsState(int gpsState) {
        this.gpsState = gpsState;
    }

    public int getAirSyncState() {
        return airSyncState;
    }

    public void setAirSyncState(int airSyncState) {
        this.airSyncState = airSyncState;
    }

    public int getAutoCfgState() {
        return autoCfgState;
    }

    public void setAutoCfgState(int autoCfgState) {
        this.autoCfgState = autoCfgState;
    }

    public int getPci() {
        return pci;
    }

    public void setPci(int pci) {
        this.pci = pci;
    }

    public String getWifiIp() {
        return wifiIp;
    }

    public void setWifiIp(String wifiIp) {
        this.wifiIp = wifiIp;
    }

    @Override
    public String toString() {
        return "DeviceInfoBean{" +
                "workState='" + workState + '\'' +
                "name='" + name + '\'' +
                ", battery=" +  (batteryUtil == null ? "" : batteryUtil.getPercent()) +
                ", temp=" + temp +
                ", gpsState=" + gpsState +
                ", airSyncState=" + airSyncState +
                ", autoCfgState=" + autoCfgState +
                ", pci=" + pci +
                ", wifiIp='" + wifiIp + '\'' +
                '}';
    }
}
