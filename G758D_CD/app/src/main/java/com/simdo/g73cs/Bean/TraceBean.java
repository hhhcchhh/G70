/**
 * 定位参数
 */
package com.simdo.g73cs.Bean;

public class TraceBean {

    public final static int RSRP_TIME_INTERVAL = 500; // 报值间隔时间

    private boolean enable, tacChange, saveOpLog;
    private String arfcn, pci, imsi, plmn, subPlmn, ueMaxTxpwr;
    private int timingOffset, airSync, bandWidth, ssbBitmap, txPwr, workState, traceRsrp, lastRsrp;
    private long cid, atCmdTimeOut, lostRsrpTime; // // 定位指令超时保护
    private int cfr, sameRsrpCnt, swap_rf;
    private int lostCount = 0;
    private String splitArfcnDl; //载波分裂频点
    private int mobRejectCode; // 0(正常)、9（强上号）
    private Long downTime = System.currentTimeMillis();
    public TraceBean(int workState) {
        this.workState = workState;
        arfcn = "";
        pci = "";
        imsi = "";
        plmn = "";
        subPlmn = "";
        ueMaxTxpwr = "10";
        airSync = 0;
        bandWidth = 20;
        ssbBitmap = 255;
        txPwr = 0;
        traceRsrp = 0; // 定位报值
        lastRsrp = -1; // 定位报值
        sameRsrpCnt = 0; // 定位报值
        atCmdTimeOut = System.currentTimeMillis(); // 指令配置时间记录
        lostRsrpTime = System.currentTimeMillis(); // 掉线起始时间
        swap_rf = 0;
        cfr = 1; //
        tacChange = false;
        enable = false;
        saveOpLog = false;
        splitArfcnDl="";
        mobRejectCode = 0;
    }

    public long getAtCmdTimeOut() {
        return atCmdTimeOut;
    }

    public void setAtCmdTimeOut(long atCmdTimeOut) {
        this.atCmdTimeOut = atCmdTimeOut;
    }

    public boolean isSaveOpLog() {
        return saveOpLog;
    }

    public void setSaveOpLog(boolean saveOpLog) {
        this.saveOpLog = saveOpLog;
    }

    public void setLastRsrp(int lastRsrp) {
        this.lastRsrp = lastRsrp;
    }

    public int getLastRsrp() {
        return lastRsrp;
    }

    public int getTraceRsrp(int id) {
        if (System.currentTimeMillis()-downTime>5000){
            traceRsrp = -1; // -1 掉线
        }
        if (traceRsrp == 0) {
            if (++lostCount >= 8) {
                lostCount = 0;
                sameRsrpCnt = 0;
                traceRsrp = -1;
            } else {
                return lastRsrp;
            }
        }
        return traceRsrp;
    }

    public void setTraceRsrp(int rsrp) {
        if (rsrp == -1) {
            sameRsrpCnt = 0;
            lostCount = 0;
            this.traceRsrp = 0;
            return;
        } else if (rsrp != 0) {
            downTime = System.currentTimeMillis();
            sameRsrpCnt = 0;
            lostCount = 0;
        }
        this.traceRsrp = rsrp;
    }

    public int getCfr() {
        return cfr;
    }

    public void setCfr(int cfr) {
        this.cfr = cfr;
    }

    public boolean isTacChange() {
        return tacChange;
    }

    public void setTacChange(boolean tacChange) {
        this.tacChange = tacChange;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getPlmn() {
        return plmn;
    }

    public void setPlmn(String plmn) {
        this.plmn = plmn;
    }

    public String getSubPlmn() {
        return subPlmn;
    }

    public void setSubPlmn(String subPlmn) {
        this.subPlmn = subPlmn;
    }

    public String getArfcn() {
        return arfcn;
    }

    public void setArfcn(String arfcn) {
        this.arfcn = arfcn;
    }

    public String getPci() {
        return pci;
    }

    public void setPci(String pci) {
        this.pci = pci;
    }

    public int getTimingOffset() {
        return timingOffset;
    }

    public void setTimingOffset(int timingOffset) {
        this.timingOffset = timingOffset;
    }

    public int getAirSync() {
        return airSync;
    }

    public void setAirSync(int airSync) {
        this.airSync = airSync;
    }

    public int getBandWidth() {
        return bandWidth;
    }

    public void setBandWidth(int bandWidth) {
        this.bandWidth = bandWidth;
    }

    public int getSsbBitmap() {
        return ssbBitmap;
    }

    public void setSsbBitmap(int ssbBitmap) {
        this.ssbBitmap = ssbBitmap;
    }

    public int getTxPwr() {
        return txPwr;
    }

    public void setTxPwr(int txPwr) {
        this.txPwr = txPwr;
    }

    public String getUeMaxTxpwr() {
        return ueMaxTxpwr;
    }

    public void setUeMaxTxpwr(String ueMaxTxpwr) {
        this.ueMaxTxpwr = ueMaxTxpwr;
    }

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public int getWorkState() {
        return workState;
    }

    public void setWorkState(int workState) {
        this.workState = workState;
    }

    public int getSwap_rf() {
        return swap_rf;
    }

    public void setSwap_rf(int swap_rf) {
        this.swap_rf = swap_rf;
    }

    public String getSplitArfcnDl() {
        return splitArfcnDl;
    }

    public void setSplitArfcnDl(String splitArfcnDl) {
        this.splitArfcnDl = splitArfcnDl;
    }

    public int getMobRejectCode() {
        return mobRejectCode;
    }

    public void setMobRejectCode(int mobRejectCode) {
        this.mobRejectCode = mobRejectCode;
    }
}
