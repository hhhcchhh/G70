package com.nr.Gnb.Response;
/**
 * typedef struct {
 *  int sync_header;
 *  int msg_type;                   //UI_2_gNB_OAM_MSG = 1000
 *  int cmd_type;                   //OAM_MSG_FREQ_SCAN_REPORT=227(in)
 *  int report_step                 //0-start, 1-data, 2-end 报告阶段
 *  int report_level;               //0-brief
 *  int scan_result;                //0-success, 1-fail
 *  int ul_arfcn;
 *  int dl_arfcn;
 *  int pci;
 *  int rsrp;
 *  int prio;                       //SIB2， not support
 *  char tac[16];
 *  char eci[16];
 *  int pk;
 *  int pa;
 *  int MCC[2];
 *  int MNC[2];
 *
 *  int sync_footer;
 * } oam_freq_scan_report_t;
 *
 */
public class GnbFreqScanRsp {
    private int reportStep;
    private int reportLevel;
    private int scanResult;
    private int ul_arfcn;
    private int dl_arfcn;
    private int pci;
    private int rsrp;
    private int prio;
    private String tac;
    private String eci;
    private int pk;
    private int pa;
    private int MCC1;
    private int MCC2;
    private int MNC1;
    private int MNC2;
    private int bandwidth;

    public GnbFreqScanRsp() {
        this.reportStep = 0;
        this.reportLevel = 0;
        this.scanResult = 0;
        this.ul_arfcn = 0;
        this.dl_arfcn = 0;
        this.pci = 0;
        this.rsrp = 0;
        this.prio = 0;
        this.tac = "0";
        this.eci = "0";
        this.pk = 0;
        this.pa = 0;
        this.MCC1 = 0;
        this.MNC1 = 0;
        this.MCC2 = 0;
        this.MNC2 = 0;
        this.bandwidth = 0;
    }

    public int getReportStep() {
        return reportStep;
    }

    public void setReportStep(int reportStep) {
        this.reportStep = reportStep;
    }

    public int getReportLevel() {
        return reportLevel;
    }

    public void setReportLevel(int reportLevel) {
        this.reportLevel = reportLevel;
    }

    public int getScanResult() {
        return scanResult;
    }

    public void setScanResult(int scanResult) {
        this.scanResult = scanResult;
    }

    public int getUl_arfcn() {
        return ul_arfcn;
    }

    public void setUl_arfcn(int ul_arfcn) {
        this.ul_arfcn = ul_arfcn;
    }

    public int getDl_arfcn() {
        return dl_arfcn;
    }

    public void setDl_arfcn(int dl_arfcn) {
        this.dl_arfcn = dl_arfcn;
    }

    public int getPci() {
        return pci;
    }

    public void setPci(int pci) {
        this.pci = pci;
    }

    public int getRsrp() {
        return rsrp;
    }

    public void setRsrp(int rsrp) {
        this.rsrp = rsrp;
    }

    public int getPrio() {
        return prio;
    }

    public void setPrio(int prio) {
        this.prio = prio;
    }

    public String getTac() {
        return tac;
    }

    public void setTac(String tac) {
        this.tac = tac;
    }

    public String getEci() {
        return eci;
    }

    public void setEci(String eci) {
        this.eci = eci;
    }

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getPa() {
        return pa;
    }

    public void setPa(int pa) {
        this.pa = pa;
    }

    public int getMCC1() {
        return MCC1;
    }

    public void setMCC1(int MCC) {
        this.MCC1 = MCC;
    }

    public int getMNC1() {
        return MNC1;
    }

    public void setMNC1(int MNC) {
        this.MNC1 = MNC;
    }

    public int getMCC2() {
        return MCC2;
    }

    public void setMCC2(int MCC2) {
        this.MCC2 = MCC2;
    }

    public int getMNC2() {
        return MNC2;
    }

    public void setMNC2(int MNC2) {
        this.MNC2 = MNC2;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    @Override
    public String toString() {
        return "GnbFreqScanRsp{" +
                "ScanResult = "+ scanResult +'\'' +
                "ReportStep='" + reportStep + '\'' +
                "tac='" + tac + '\'' +
                ", eci='" + eci + '\'' +
                ", ul_arfcn=" + ul_arfcn +
                ", dl_arfcn=" + dl_arfcn +
                ", pci=" + pci +
                ", rsrp=" + rsrp +
                ", pk=" + pk +
                ", pa=" + pa +
                ", mcc/mnc[1]: " + MCC1 +
                ", " + MNC1 +
                ", mcc/mnc[2]:" + MCC2 +
                ", " + MNC2 +
                ", bandwidth=" + bandwidth +
                '}';
    }
}
