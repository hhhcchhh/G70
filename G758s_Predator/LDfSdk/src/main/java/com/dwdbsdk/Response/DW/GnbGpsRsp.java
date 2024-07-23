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

public class GnbGpsRsp {

    int gnssSelect;
    int latitude;
    int longitude;
    int gpsState;
    int gpsTDay;
    int gpsTTime;

    public GnbGpsRsp() {
        this.gnssSelect = 0;
        this.latitude = 0;
        this.longitude = 0;
        this.gpsState = 0;
        this.gpsTDay = 0;
        this.gpsTTime = 0;
    }

    public int getGnssSelect() {
        return gnssSelect;
    }

    public void setGnssSelect(int gnssSelect) {
        this.gnssSelect = gnssSelect;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public int getGpsState() {
        return gpsState;
    }

    public void setGpsState(int gpsState) {
        this.gpsState = gpsState;
    }

    public int getGpsTDay() {
        return gpsTDay;
    }

    public void setGpsTDay(int gpsTDay) {
        this.gpsTDay = gpsTDay;
    }

    public int getGpsTTime() {
        return gpsTTime;
    }

    public void setGpsTTime(int gpsTTime) {
        this.gpsTTime = gpsTTime;
    }

    @Override
    public String toString() {
        return "GnbGpsRsp{" +
                "gnssSelect=" + gnssSelect +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", gpsState=" + gpsState +
                ", gpsTDay=" + gpsTDay +
                ", gpsTTime=" + gpsTTime +
                '}';
    }

}
