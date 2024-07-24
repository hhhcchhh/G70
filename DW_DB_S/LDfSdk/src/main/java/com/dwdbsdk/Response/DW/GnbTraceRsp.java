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
    int rnti;

    int phone_type;
    int cellId;
    List<String> imsiList; // 此次读到的IMSI列表
    List<String> gutiList; // 此次读到的IMSI列表
    GnbCmdRsp cmdRsp; // 指令反馈

    public GnbTraceRsp() {
        this.rsrp = 0;
        this.rssi = 0;
        this.distance = 0;
        this.cellId = -1;
        this.cmdRsp = null;
        this.imsiList = new ArrayList<>();
        this.gutiList = new ArrayList<>();
    }
    public int getPhone_type() {
        return phone_type;
    }

    public void setPhone_type(int phone_type) {
        this.phone_type = phone_type;
    }

    public int getRnti() {
        return rnti;
    }

    public void setRnti(int rnti) {
        this.rnti = rnti;
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

    public List<String> getImsiList() {
        return imsiList;
    }

    public void addImsi(String imsi) {
        this.imsiList.add(imsi);
    }

    public List<String> getGutiList() {
        return gutiList;
    }

    public void addGuti(String guti) {
        this.gutiList.add(guti);
    }

    public void setImsiList(List<String> imsiList) {
        this.imsiList = imsiList;
    }
    public void setGutiList(List<String> gutiList) {
        this.gutiList = gutiList;
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

    private String gutiStr() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < gutiList.size(); i++) {
            sb.append(gutiList.get(0));
            sb.append(";");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GnbTraceRsp{");
        sb.append("cellId = ").append(cellId);
        sb.append(", rsrp = ").append(rsrp);
        sb.append(", rssi = ").append(rssi);
        sb.append(", distance = ").append(distance);
        sb.append(", rnti = ").append(rnti);
        sb.append(", imsiList = ").append(imsiStr());
        sb.append(", gutiList = ").append(gutiStr());
        if (cmdRsp != null) {
            sb.append(", cmdRsp = ").append(cmdRsp.toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
