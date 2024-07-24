package com.simdo.dw_4db_s.Bean;

import com.simdo.dw_4db_s.Util.BatteryUtil;

public class DeviceInfoBean {

    String id = "";
    //_f,_r,_b,_l   前右后左对应设备0,1,2,3
    String name = "待连接..";
    BatteryUtil batteryUtil = new BatteryUtil();
    String temp = "0";
    String rxGain = "标准";
    int location = 0;
    int gpsState = 0;
    int airSyncState = 2;
    int autoCfgState = 0;
    int pci = 0;
    String wifiIp = "";
    int lastMsgSn = 0;

    public int getLostCount() {
        return lostCount;
    }

    public void setLostCount(int lostCount) {
        this.lostCount = lostCount;
    }

    int lostCount = 0;
    int workState = GnbBean.DB_State.NONE;
    public String getRxGain() {
        return rxGain;
    }

    public void setRxGain(String rxGain) {
        this.rxGain = rxGain;
    }
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
    public DeviceInfoBean() {
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

    public BatteryUtil getBatteryUtil() {
        return batteryUtil;
    }

    public void setBatteryUtil(BatteryUtil batteryUtil) {
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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
