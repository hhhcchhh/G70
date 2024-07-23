/**
 * 定位侦码Rsp
 */
package com.dwdbsdk.Response.DW;

import java.util.ArrayList;
import java.util.List;

public class GnbTraceRsp {
    int rsrp; // 定位报值
    int rssi; // 定位报值
    int distance; // 定位报值
    int cellId;
    List<String> imsiList; // 此次读到的IMSI列表
    GnbCmdRsp cmdRsp; // 指令反馈
    private int rnti;
    int phone_type;

    public GnbTraceRsp() {
        this.rsrp = 0;
        this.rssi = 0;
        this.distance = 0;
        this.cellId = -1;
        this.cmdRsp = null;
        this.imsiList = new ArrayList<>();
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getRsrp() {
        return rsrp;
    }

    public void setRsrp(int rsrp) {
        this.rsrp = rsrp;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
    public int getRnti() {
        return rnti;
    }

    public void setRnti(int rnti) {
        this.rnti = rnti;
    }

    public List<String> getImsiList() {
        return imsiList;
    }

    public void addImsi(String imsi) {
        this.imsiList.add(imsi);
    }

    public void setImsiList(List<String> imsiList) {
        this.imsiList = imsiList;
    }

    public int getPhone_type() {
        return phone_type;
    }

    public void setPhone_type(int phone_type) {
        this.phone_type = phone_type;
    }

    public GnbCmdRsp getCmdRsp() {
        return cmdRsp;
    }

    public void setCmdRsp(GnbCmdRsp cmdRsp) {
        this.cmdRsp = cmdRsp;
    }

    private String imsiStr() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < imsiList.size(); i++) {
            sb.append(imsiList.get(0));
            sb.append(";");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GnbTraceRsp{");
        sb.append("cellId = " + cellId);
        sb.append(", rsrp = " + rsrp);
        sb.append(", rssi = " + rssi);
        sb.append(", distance = " + distance);
        sb.append(", imsiList = " + imsiStr());
        if (cmdRsp != null) {
            sb.append(", cmdRsp = " + cmdRsp.toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
