/**
 * 心跳数据 与 配置信息反馈
 * typedef struct {
 * int sync_header;
 * int msg_type;          		//UI_2_gNB_OAM_MSG
 * int cmd_type;             	//UI_2_gNB_HEART_BEAT
 * int cmd_param;
 * <p>
 * int gnb_state[2]; 			// gnb_state_e, tx1 and tx2
 * int gps_sync_state; 		    // timing_state_e gps sync: 1, gps outof sync: 0
 * int time_sync_state; 		// whether gnb_set_time_t done
 * int air_sync_state; 		    // 0：空闲；1：成功；2：失败 两小区状态分别由高低部分来表示, 如：01 00 02 00（低位在前高位在后） 小区一状态：低位（01 00）0x0001 小区二状态：高位（02 00）0x0002
 * char wifi_ip[OAM_STR_MAX];
 * char wifi_ssid[OAM_STR_MAX];
 * char dev_id[36];
 * char bt_name[OAM_STR_MAX];
 * int voltage[4];				//real*1000
 * int temp[4];           		//real*1000
 * int work_mode;				//1-portable 2-vehicle 3-fence 4-fence.scan
 * int dev_state;				//非 0：abnormal(设备异常) 0：normal（设备正常）1、定位、侦码等业务功能异常 2、升级、取 LOG 等常规功能正常工作
 * char dev_name[OAM_STR_MAX];	//from OAM_MSG_SET_SYS_INFO
 * int longitude;			//real*1000
 * int latitude;			//real*1000
 * int dual_cell;    		//1-single, 2-dual
 * gnssSelect              //0-gps+北斗，1-gps， 2-北斗   OAM_MSG_SET_GPS_CFG= 211
 * sysKickOff              //0-init，1-kickoff   状态等于 0，系统处于启动中，不处理 指令
 * fanSpeed              //0-gps+北斗，1-gps， 2-北斗   OAM_MSG_SET_FAN_SPEED= 216
 * int sync_footer;
 * } oam_state_report_t; //out
 */
package com.dwdbsdk.Response.DW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GnbStateRsp {
    //Bellow for heart state
    private int firstState; // 工作状态: 详见类：gnbState
    private int secondState; // 工作状态: 详见类：gnbState
    private int thirdState; // 工作状态: 详见类：gnbState
    private int fourthState; // 工作状态: 详见类：gnbState
    private int gpsSyncState; // GPS 同步情况 1： 同步， 0：失步，
    private int firstAirState;  //空口同步： 0：空闲；1：成功；2：失败
    private int secondAirState;  //空口同步： 0：空闲；1：成功；2：失败
    private int thirdAirState;  //空口同步： 0：空闲；1：成功；2：失败
    private int fourthAirState;  //空口同步： 0：空闲；1：成功；2：失败
    private int timeSetState;  //是否配置单板时间 1： 配置， 0：未配置
    private int workMode;		// 1-portable, 2-vehicle 3- fence 4-fence.scan
    private int devState; 		// 0-abnormal, 1-normal
    private int dualCell; 		// 1-single, 2-dual
    private int dualStack;        // 0-single stack，1-dual stack
    private int gnss_select = 0; // 0-GpsAndBeidou, 1-GPS, 2-Beidou
    private int sysKickOff = 0;
    private int[] fanSpeed;
    private Double longitude; 		//
    private Double latitude; 		//
    private String ssid; // 便携连接WIFI热点名称
    private String wifiIp; // 便携返回连接IP地址，车载不关注
    private String btName; // 便携连接蓝牙名称
    private String deviceId; // 设备ID值：区分单板用，定位时需匹配此ID
    private String devName; // 设备名称
    private List<Double> voltageList = new ArrayList<Double>(); // 电池电压
    private List<Double> tempList = new ArrayList<Double>(); //Temp[0]为 FPGA核心温度 Temp[1]为主控核心温度


    public GnbStateRsp(int firstCell, int secondCell, int gpsSyncState, int timeSyncState, int fSyncState, int sSyncState, String wifiIp, String ssid,
                       String deviceId, String btName) {
        this.firstState = firstCell;
        this.secondState = secondCell;
        this.gpsSyncState = gpsSyncState;
        this.timeSetState = timeSyncState;
        this.firstAirState = fSyncState;    //通道一空口状态
        this.secondAirState = sSyncState;   //通道二空口状态

        this.longitude = 0.0;
        this.latitude = 0.0;
        this.devState = 1;
        this.workMode = 1;
        this.dualCell = 2;
        this.wifiIp = wifiIp;
        this.ssid = ssid;
        this.deviceId = deviceId;
        this.btName = btName;
        this.devName = "";

        this.voltageList.clear();
        this.tempList.clear();
    }

    public int getDualCell() {
        return dualCell;
    }

    public void setDualCell(int dualCell) {
        this.dualCell = dualCell;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public int getWorkMode() {
        return workMode;
    }

    public void setWorkMode(int workMode) {
        this.workMode = workMode;
    }

    public int getDevState() {
        return devState;
    }

    public void setDevState(int devState) {
        this.devState = devState;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public int getThirdState() {
        return thirdState;
    }

    public void setThirdState(int thirdState) {
        this.thirdState = thirdState;
    }

    public int getFourthState() {
        return fourthState;
    }

    public void setFourthState(int fourthState) {
        this.fourthState = fourthState;
    }

    public int getThirdAirState() {
        return thirdAirState;
    }

    public void setThirdAirState(int thirdAirState) {
        this.thirdAirState = thirdAirState;
    }

    public int getFourthAirState() {
        return fourthAirState;
    }

    public void setFourthAirState(int fourthAirState) {
        this.fourthAirState = fourthAirState;
    }

    public int getDualStack() {
        return dualStack;
    }

    public void setDualStack(int dualStack) {
        this.dualStack = dualStack;
    }

    public void addVol(int vol) {
        double dvol = Math.round(vol/10.0); // 4舍5入
        dvol = dvol/100.0;
        this.voltageList.add(dvol);
    }

    public void addTemp(int temp) {
        double dtemp = Math.round(temp/10.0);
        dtemp = dtemp/100.0;
        this.tempList.add(dtemp);
    }

    public List<Double> getVoltageList() {
        return voltageList;
    }

    public List<Double> getTempList() {
        return tempList;
    }

    public String getBtName() {
        return btName;
    }

    public int getFirstState() {
        return firstState;
    }

    public int getSecondState() {
        return secondState;
    }

    public int getGpsSyncState() {
        return gpsSyncState;
    }

    public int getTimeSetState() {
        return timeSetState;
    }

    public int getFirstAirState() {
        return firstAirState;
    }

    public void setFirstAirState(int firstAirState) {
        this.firstAirState = firstAirState;
    }

    public int getSecondAirState() {
        return secondAirState;
    }

    public void setSecondAirState(int secondAirState) {
        this.secondAirState = secondAirState;
    }

    public String getWifiIp() {
        return wifiIp;
    }

    public String getSsid() {
        return ssid;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getGnss_select() {
        return gnss_select;
    }

    public void setGnss_select(int gnss_select) {
        this.gnss_select = gnss_select;
    }
    public int getSysKickOff() {
        return sysKickOff;
    }

    public void setSysKickOff(int sysKickOff) {
        this.sysKickOff = sysKickOff;
    }

    public int[] getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(int[] fanSpeed) {
        this.fanSpeed = fanSpeed;
    }
    @Override
    public String toString() {
        return "GnbStateRsp{" +
                "firstState=" + firstState +
                ", secondState=" + secondState +
                ", thirdState=" + thirdState +
                ", fourthState=" + fourthState +
                ", gpsSyncState=" + gpsSyncState +
                ", firstAirState=" + firstAirState +
                ", secondAirState=" + secondAirState +
                ", thirdAirState=" + thirdAirState +
                ", fourthAirState=" + fourthAirState +
                ", timeSetState=" + timeSetState +
                ", workMode=" + workMode +
                ", devState=" + devState +
                ", dualCell=" + dualCell +
                ", dualStack=" + dualStack +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", ssid='" + ssid + '\'' +
                ", wifiIp='" + wifiIp + '\'' +
                ", btName='" + btName + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", devName='" + devName + '\'' +
                ", gnssSelect=" + gnss_select +
                ", sysKickOff=" + sysKickOff +
                ", fanSpeed=" + Arrays.toString(fanSpeed) +
                ", voltageList=" + voltageList +
                ", tempList=" + tempList +
                '}';
    }
    //空口同步：1： 同步， 0：失败
    public class Air {
        public final static int SUCC = 1; //
        public final static int FAIL = 0;
    }
    //0-out of sync, 1-sync
    public class Gps {
        public final static int FAIL = 0; //
        public final static int SUCC = 1; //
    }
    //0-out of sync, 1-sync
    public class devState {
        public final static int ABNORMAL = 1; //
        public final static int NORMAL = 0; //
    }
    public class gnssSelect {
        public final static int GPSANDBEIDOU = 0; //GPS+北斗
        public final static int GPS = 1; // GPS
        public final static int BEIDOU = 2; //北斗
    }
    // firstState ,secondState
    public class gnbState {
        public static final int GNB_STATE_WAIT_CFG = 0;           // 等待配置
        public static final int GNB_STATE_UPGRADING = 1;          // 升级中，NO USE
        public static final int GNB_STATE_CATCH = 2;              // 抓号中
        public static final int GNB_STATE_TRACE = 3;              // 追踪中
        public static final int GNB_STATE_CONTROL = 4;            // 管控中
        public static final int GNB_STATE_PHY_ABNORMAL = 0xFF;     // PHY异常
        public static final int GNB_STATE_AUTO_CFG = 0xF0;     // 侦码自启动配置成功
        public static final int GNB_STATE_CFG_FAIL = 0xF1;     // 侦码自启动配置失败
    }
}
