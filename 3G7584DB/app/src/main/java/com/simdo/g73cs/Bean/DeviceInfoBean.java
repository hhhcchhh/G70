package com.simdo.g73cs.Bean;

import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Response.DW.GnbStateRsp;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.TraceUtil;

public class DeviceInfoBean {
    private String license, softVer, fpgaVer, hwVer; // 基带板密匙、软件版本、逻辑版本、硬件版本
    private int workState; // 当前工作状态
    private GnbStateRsp rsp; // 心跳信息
    private TraceUtil traceUtil;

    public DeviceInfoBean() {
        rsp = null;
        traceUtil = new TraceUtil();
        workState = GnbBean.DW_State.IDLE;
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
        AppLog.D("setWorkState = " + workState);
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

    public Double getMaxTemp() {
        Double temp = 0d;
        for (Double d : rsp.getTempList()) {
            if (d > temp) temp = d;
        }
        return temp;
    }

    public void initTraceUtil(int logicIndex) {
        getTraceUtil().setTacChange(DWProtocol.CellId.FIRST, false);
        getTraceUtil().setTxPwr(DWProtocol.CellId.FIRST, 0);
        getTraceUtil().setCfr(DWProtocol.CellId.FIRST, 1);
        getTraceUtil().setUeMaxTxpwr(DWProtocol.CellId.FIRST, "10");
        getTraceUtil().setCid(DWProtocol.CellId.FIRST, logicIndex * 4L + 65536);
        getTraceUtil().setSplit_arfcn_dl(DWProtocol.CellId.FIRST, "0");

        getTraceUtil().setTacChange(DWProtocol.CellId.SECOND, false);
        getTraceUtil().setTxPwr(DWProtocol.CellId.SECOND, 0);
        getTraceUtil().setCfr(DWProtocol.CellId.SECOND, 1);
        getTraceUtil().setUeMaxTxpwr(DWProtocol.CellId.SECOND, "10");
        getTraceUtil().setCid(DWProtocol.CellId.SECOND, logicIndex * 4L + 65537);
        getTraceUtil().setSplit_arfcn_dl(DWProtocol.CellId.SECOND, "0");

        getTraceUtil().setTacChange(DWProtocol.CellId.THIRD, false);
        getTraceUtil().setTxPwr(DWProtocol.CellId.THIRD, 0);
        getTraceUtil().setCfr(DWProtocol.CellId.THIRD, 1);
        getTraceUtil().setUeMaxTxpwr(DWProtocol.CellId.THIRD, "10");
        getTraceUtil().setCid(DWProtocol.CellId.THIRD, logicIndex * 4L + 65538);
        getTraceUtil().setSplit_arfcn_dl(DWProtocol.CellId.THIRD, "0");

        getTraceUtil().setTacChange(DWProtocol.CellId.FOURTH, false);
        getTraceUtil().setTxPwr(DWProtocol.CellId.FOURTH, 0);
        getTraceUtil().setCfr(DWProtocol.CellId.FOURTH, 1);
        getTraceUtil().setUeMaxTxpwr(DWProtocol.CellId.FOURTH, "10");
        getTraceUtil().setCid(DWProtocol.CellId.FOURTH, logicIndex * 4L + 65539);
        getTraceUtil().setSplit_arfcn_dl(DWProtocol.CellId.FOURTH, "0");

    }
}
