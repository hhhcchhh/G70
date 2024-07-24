package com.simdo.dw_4db_s.Bean;

import com.dwdbsdk.Response.DW.GnbStateRsp;
import com.simdo.dw_4db_s.Util.TraceUtil;

import java.util.Objects;

public class DwDeviceInfoBean {
    private String license, softVer, fpgaVer, hwVer; // 基带板密匙、软件版本、逻辑版本、硬件版本
    private int workState; // 当前工作状态
    private GnbStateRsp rsp; // 心跳信息
    private TraceUtil traceUtil;  //定位相关状态
    private String id; // 设备ID
    public DwDeviceInfoBean() {
        rsp = null;
        traceUtil = new TraceUtil();
        workState = GnbBean.DW_State.NONE;
        license = "";
        softVer = "";
        fpgaVer = "";
        hwVer = "";
        id = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DwDeviceInfoBean)) return false;
        DwDeviceInfoBean that = (DwDeviceInfoBean) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
