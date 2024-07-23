/**
 * FTP查询反馈
 * typedef struct {
    int sync_header;
    int msg_type;      		//UI_2_gNB_OAM_MSG
    int cmd_type;    		//OAM_MSG_GET_FTP_SERVER
    int cmd_param;

    char ftp_server[OAM_STR_MAX]; 192.168.1.100
    char ftp_path[OAM_STR_MAX]; g70_ftp
    char ftp_user[OAM_STR_MAX]; user
    char ftp_passwd[OAM_STR_MAX]; admin
    int upload_interval
    int sync_footer;
 * } oam_get_meth_t; 	//out
 */
package com.nr.Gnb.Response;

public class GnbFtpRsp {
    int uploadInterval;
    String ftpServer, ftpPath, ftpUser, ftpPasswd; // 基带版本

    public GnbFtpRsp() {
        this.ftpServer = "";
        this.ftpPath = "";
        this.ftpUser = "";
        this.ftpPasswd = "";
        this.uploadInterval = 0;
    }

    public int getUploadInterval() {
        return uploadInterval;
    }

    public void setUploadInterval(int uploadInterval) {
        this.uploadInterval = uploadInterval;
    }

    public String getFtpServer() {
        return ftpServer;
    }

    public void setFtpServer(String ftpServer) {
        this.ftpServer = ftpServer;
    }

    public String getFtpPath() {
        return ftpPath;
    }

    public void setFtpPath(String ftpPath) {
        this.ftpPath = ftpPath;
    }

    public String getFtpUser() {
        return ftpUser;
    }

    public void setFtpUser(String ftpUser) {
        this.ftpUser = ftpUser;
    }

    public String getFtpPasswd() {
        return ftpPasswd;
    }

    public void setFtpPasswd(String ftpPasswd) {
        this.ftpPasswd = ftpPasswd;
    }

    @Override
    public String toString() {
        return "GnbFtpRsp{" +
                ", ftpServer='" + ftpServer + '\'' +
                ", ftpPath='" + ftpPath + '\'' +
                ", ftpUser='" + ftpUser + '\'' +
                ", ftpPasswd='" + ftpPasswd + '\'' +
                ", UploadInterval='" + uploadInterval +
                '}';
    }
}
