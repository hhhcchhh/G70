package com.dwdbsdk.Response.DW;
/**
 * typedef struct {
 *  int sync_header;
 *  int msg_type;                   //UI_2_gNB_OAM_MSG = 1000
 *  int cmd_type;                   //OAM_MSG_FREQ_SCAN_REPORT=227(in)
 *  int report_step                 //0-start, 1-data, 2-end 报告阶段
 *  int report_level;               //0-brief
 *  int scan_result;                //0-success, 1-fail
 *  file_name[64]                   举例：sweepInfo_1102_14_51_02.zi
 *  int sync_footer;
 * } oam_freq_scan_report_t;
 *
 */
public class GnbFreqScanGetDocumentRsp {
    private int reportStep;
    private int reportLevel;
    private int scanResult;
    private String fileName;

    public GnbFreqScanGetDocumentRsp() {
        this.reportStep = 0;
        this.reportLevel = 1;
        this.scanResult = 0;
        this.fileName = "";
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "GnbFreqScanGetDocumentRsp{" +
                "reportStep=" + reportStep +
                ", reportLevel=" + reportLevel +
                ", scanResult=" + scanResult +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
