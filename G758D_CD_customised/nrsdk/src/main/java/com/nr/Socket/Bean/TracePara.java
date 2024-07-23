package com.nr.Socket.Bean;

public class TracePara {
    private String id, plmn, arfcn, pci, ueMaxTxpwr, plmn1, imsi, splitArfcndl;
    private int cellId, startTac, maxTac, runTac, timingOffset, workMode, airSync, ulRbOffset, ssbBitmap, traceTacChangeDelay, stopCount, bandWidth, cfr, swapRf;
    private int rejectCode, rxLevMin, pa, pk, mobRejectCode, redirect2LteArfcn, forceCfg;
    private long cid;
    private int tacInterval;
    private boolean tracing, enableChangeTac;
    boolean isLte;
    public TracePara(String id, boolean isLte,int cellId, String imsi, String plmn, String arfcn, String pci, String ue_max_txpwr, int startTac, int maxTac, int timingOffset,
                     int work_mode, int airSync, String plmn1, int ul_rb_offset, long cid, int ssb_bitmap, int bandWidth, int cfrEnable, int swap_rf,
                     int reject_code, int rxLevMin, int redirect_2_4g_earfcn, int mob_reject_code,String split_arfcn_dl, int forceCfg) {
        this.id = id;
        this.isLte =isLte;
        this.cellId = cellId;
        this.plmn = plmn;
        this.arfcn = arfcn;
        this.pci = pci;
        this.ueMaxTxpwr = ue_max_txpwr;
        this.startTac = startTac;
        this.maxTac = maxTac;
        this.timingOffset = timingOffset;
        this.workMode = work_mode;
        this.airSync = airSync;
        this.plmn1 = plmn1;
        this.ulRbOffset = ul_rb_offset;
        this.cid = cid;
        this.ssbBitmap = ssb_bitmap;
        this.bandWidth = bandWidth;
        this.cfr = cfrEnable;
        this.swapRf = swap_rf;
        this.rejectCode = reject_code;
        this.rxLevMin = rxLevMin;
        this.pk = 0;
        this.pa = 0;
        this.redirect2LteArfcn = redirect_2_4g_earfcn;
        this.mobRejectCode = mob_reject_code;
        this.splitArfcndl = split_arfcn_dl;
        this.tracing = false;
        this.enableChangeTac = false;
        this.imsi = imsi;
        this.traceTacChangeDelay = 0;
        this.stopCount = 0;
        this.forceCfg = forceCfg;
        this.runTac = 6;
        this.tacInterval = 9;
    }
    public int getTacInterval() {
        return tacInterval;
    }

    public void setTacInterval(int tacInterval) {
        this.tacInterval = tacInterval;
    }
    public int getRunTac() {
        return runTac;
    }

    public void setRunTac(int runTac) {
        this.runTac = runTac;
    }
    public int getForceCfg() {
        return forceCfg;
    }

    public void setForceCfg(int forceCfg) {
        this.forceCfg = forceCfg;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isLte() {
        return isLte;
    }

    public void setLte(boolean lte) {
        isLte = lte;
    }

    public int getRejectCode() {
        return rejectCode;
    }

    public void setRejectCode(int rejectCode) {
        this.rejectCode = rejectCode;
    }

    public int getRxLevMin() {
        return rxLevMin;
    }

    public void setRxLevMin(int rxLevMin) {
        this.rxLevMin = rxLevMin;
    }

    public int getSwapRf() {
        return swapRf;
    }

    public void setSwapRf(int swapRf) {
        this.swapRf = swapRf;
    }

    public boolean isEnableChangeTac() {
        return enableChangeTac;
    }

    public void setEnableChangeTac(boolean enableChangeTac) {
        this.enableChangeTac = enableChangeTac;
    }

    public int getCfr() {
        return cfr;
    }

    public void setCfr(int cfr) {
        this.cfr = cfr;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public boolean isTracing() {
        return tracing;
    }

    public void setTracing(boolean tracing) {
        this.tracing = tracing;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public String getPlmn() {
        return plmn;
    }

    public void setPlmn(String plmn) {
        this.plmn = plmn;
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

    public String getUeMaxTxpwr() {
        return ueMaxTxpwr;
    }

    public void setUeMaxTxpwr(String ueMaxTxpwr) {
        this.ueMaxTxpwr = ueMaxTxpwr;
    }

    public String getPlmn1() {
        return plmn1;
    }

    public void setPlmn1(String plmn1) {
        this.plmn1 = plmn1;
    }

    public int getStartTac() {
        return startTac;
    }

    public void setStartTac(int startTac) {
        this.startTac = startTac;
    }

    public int getMaxTac() {
        return maxTac;
    }

    public void setMaxTac(int maxTac) {
        this.maxTac = maxTac;
    }

    public int getTimingOffset() {
        return timingOffset;
    }

    public void setTimingOffset(int timingOffset) {
        this.timingOffset = timingOffset;
    }

    public int getWorkMode() {
        return workMode;
    }

    public void setWorkMode(int workMode) {
        this.workMode = workMode;
    }

    public int getAirSync() {
        return airSync;
    }

    public void setAirSync(int airSync) {
        this.airSync = airSync;
    }

    public int getUlRbOffset() {
        return ulRbOffset;
    }

    public void setUlRbOffset(int ulRbOffset) {
        this.ulRbOffset = ulRbOffset;
    }

    public int getSsbBitmap() {
        return ssbBitmap;
    }

    public void setSsbBitmap(int ssbBitmap) {
        this.ssbBitmap = ssbBitmap;
    }

    public int getTraceTacChangeDelay() {
        return traceTacChangeDelay;
    }

    public void setTraceTacChangeDelay(int traceTacChangeDelay) {
        this.traceTacChangeDelay = traceTacChangeDelay;
    }

    public int getStopCount() {
        return stopCount;
    }

    public void setStopCount(int stopCount) {
        this.stopCount = stopCount;
    }

    public int getBandWidth() {
        return bandWidth;
    }

    public void setBandWidth(int bandWidth) {
        this.bandWidth = bandWidth;
    }

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public int getPa() {
        return pa;
    }

    public void setPa(int pa) {
        this.pa = pa;
    }

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getMobRejectCode() {
        return mobRejectCode;
    }

    public void setMobRejectCode(int mobRejectCode) {
        this.mobRejectCode = mobRejectCode;
    }

    public int getRedirect2LteArfcn() {
        return redirect2LteArfcn;
    }

    public void setRedirect2LteArfcn(int redirect2LteArfcn) {
        this.redirect2LteArfcn = redirect2LteArfcn;
    }

    public String getSplitArfcndl() {
        return splitArfcndl;
    }

    public void setSplitArfcndl(String splitArfcndl) {
        this.splitArfcndl = splitArfcndl;
    }

    @Override
    public String toString() {
        return "TracePara{" +
                "isLte='" + isLte + '\'' +
                "plmn='" + plmn + '\'' +
                ", arfcn='" + arfcn + '\'' +
                ", pci='" + pci + '\'' +
                ", ueMaxTxpwr='" + ueMaxTxpwr + '\'' +
                ", plmn1='" + plmn1 + '\'' +
                ", imsi='" + imsi + '\'' +
                ", cellId=" + cellId +
                ", startTac=" + startTac +
                ", maxTac=" + maxTac +
                ", timingOffset=" + timingOffset +
                ", workMode=" + workMode +
                ", airSync=" + airSync +
                ", ulRbOffset=" + ulRbOffset +
                ", ssbBitmap=" + ssbBitmap +
                ", traceTacChangeDelay=" + traceTacChangeDelay +
                ", stopCount=" + stopCount +
                ", bandWidth=" + bandWidth +
                ", cfr=" + cfr +
                ", swapRf=" + swapRf +
                ", rejectCode=" + rejectCode +
                ", rxLevMin=" + rxLevMin +
                ", pa=" + pa +
                ", pk=" + pk +
                ", mobRejectCode=" + mobRejectCode +
                ", redirect2LteArfcn=" + redirect2LteArfcn +
                ", cid=" + cid +
                ", tracing=" + tracing +
                ", enableChangeTac=" + enableChangeTac +
                ", id='" + id + '\'' +
                ", splitArfcndl='" + splitArfcndl + '\'' +
                '}';
    }
}
