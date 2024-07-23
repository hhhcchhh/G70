package com.simdo.g73cs.Bean;

import com.nr.Gnb.Response.GnbStateRsp;
import com.simdo.g73cs.Util.TraceUtil;

public class DeviceInfoBean {
    private String license, softVer, fpgaVer, hwVer; // 基带板密匙、软件版本、逻辑版本、硬件版本
    private int workState; // 当前工作状态
    private GnbStateRsp rsp; // 心跳信息
    private TraceUtil traceUtil;
    public DeviceInfoBean() {
        rsp = null;
        traceUtil = new TraceUtil();
        workState = GnbBean.State.IDLE;
        license = "";
        softVer = "";
        fpgaVer = "";
        hwVer = "";
    }
    public TraceUtil getTraceUtil() {
        return traceUtil;
    }

    public void setTraceUtil(TraceUtil traceUtil) {
        this.traceUtil = traceUtil;
    }
    public int getWorkState() {
        return workState;
    }

    public void setWorkState(int workState) {
        this.workState = workState;
    }
    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
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

    public GnbStateRsp getRsp() {
        return rsp;
    }

    public void setRsp(GnbStateRsp rsp) {
        this.rsp = rsp;
    }

    public Double getMaxTemp(){
        Double temp = 0d;
        for (Double d : rsp.getTempList()) {
            if (d > temp) temp = d;
        }
        return temp;
    }
}
