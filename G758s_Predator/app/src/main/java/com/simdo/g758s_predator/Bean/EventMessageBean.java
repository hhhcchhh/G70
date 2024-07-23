package com.simdo.g758s_predator.Bean;

import java.util.ArrayList;
import java.util.List;

public class EventMessageBean {
    private int what;
    private String msg;
    private String string;
    private int airEnable;
    private DwDeviceInfoBean dwDeviceInfoBean;
    private List<Integer> arfcnList_N1 = new ArrayList<>();
    private List<Integer> arfcnList_N28 = new ArrayList<>();
    private List<Integer> arfcnList_N41 = new ArrayList<>();
    private List<Integer> arfcnList_N78 = new ArrayList<>();
    private List<Integer> arfcnList_N79 = new ArrayList<>();
    private List<Integer> chanelList = new ArrayList<>();
    private List<Boolean> enablelList = new ArrayList<>();
    private List<ScanArfcnBean> reportList = new ArrayList();

    public DwDeviceInfoBean getDwDeviceInfoBean() {
        return dwDeviceInfoBean;
    }

    public void setDwDeviceInfoBean(DwDeviceInfoBean dwDeviceInfoBean) {
        this.dwDeviceInfoBean = dwDeviceInfoBean;
    }
    public EventMessageBean() {
    }
    public EventMessageBean(String msg) {
        this.msg = msg;
    }
    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public int getWhat() {
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<ScanArfcnBean> getReportList() {
        return reportList;
    }

    public void setReportList(List<ScanArfcnBean> reportList) {
        this.reportList = reportList;
    }

    public int getAirEnable() {
        return airEnable;
    }

    public void setAirEnable(int airEnable) {
        this.airEnable = airEnable;
    }

    public List<Integer> getArfcnList_N1() {
        return arfcnList_N1;
    }

    public void setArfcnList_N1(List<Integer> arfcnList_N1) {
        this.arfcnList_N1 = arfcnList_N1;
    }

    public List<Integer> getArfcnList_N28() {
        return arfcnList_N28;
    }

    public void setArfcnList_N28(List<Integer> arfcnList_N28) {
        this.arfcnList_N28 = arfcnList_N28;
    }

    public List<Integer> getArfcnList_N41() {
        return arfcnList_N41;
    }

    public void setArfcnList_N41(List<Integer> arfcnList_N41) {
        this.arfcnList_N41 = arfcnList_N41;
    }

    public List<Integer> getArfcnList_N78() {
        return arfcnList_N78;
    }

    public void setArfcnList_N78(List<Integer> arfcnList_N78) {
        this.arfcnList_N78 = arfcnList_N78;
    }

    public List<Integer> getArfcnList_N79() {
        return arfcnList_N79;
    }

    public void setArfcnList_N79(List<Integer> arfcnList_N79) {
        this.arfcnList_N79 = arfcnList_N79;
    }

    public List<Integer> getChanelList() {
        return chanelList;
    }

    public void setChanelList(List<Integer> chanelList) {
        this.chanelList = chanelList;
    }

    public List<Boolean> getEnablelList() {
        return enablelList;
    }

    public void setEnablelList(List<Boolean> enablelList) {
        this.enablelList = enablelList;
    }
}
