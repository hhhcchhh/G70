/**
 * FTP查询反馈
 * typedef struct {
    int sync_header;
    int msg_type;      		//UI_2_gNB_OAM_MSG
    int cmd_type;    		//OAM_MSG_GET_FTP_SERVER
    int cmd_param;

    char ftp_server[OAM_STR_MAX]; 192.168.1.100
    char ftp_path[OAM_STR_MAX]; DW_ftp
    char ftp_user[OAM_STR_MAX]; user
    char ftp_passwd[OAM_STR_MAX]; admin
    int upload_interval
    int sync_footer;
 * } oam_get_meth_t; 	//out
 */
package com.dwdbsdk.Response.DW;

public class GnbGpsInOutRsp {

    int outGpioIdx;
    int inGpioIdx;

    public GnbGpsInOutRsp() {
        this.outGpioIdx = 0;
        this.inGpioIdx = 0;
    }

    public int getOutGpioIdx() {
        return outGpioIdx;
    }

    public void setOutGpioIdx(int outGpioIdx) {
        this.outGpioIdx = outGpioIdx;
    }

    public int getInGpioIdx() {
        return inGpioIdx;
    }

    public void setInGpioIdx(int inGpioIdx) {
        this.inGpioIdx = inGpioIdx;
    }

    @Override
    public String toString() {
        return "GnbGpsInOutRsp{" +
                "outGpioIdx=" + outGpioIdx +
                ", inGpioIdx=" + inGpioIdx +
                '}';
    }

}
