/**
 * 心跳数据 与 配置信息反馈
 */
package com.dwdbsdk.Response.DB;

public class MsgStateRsp {
	int msgSn;							//serial num
	int msgLen;						//sizeof bt_msg_xxx_t
	int msgType;						//MessageProtocol.MsgType
	// for heart msg
	int param;
	int workMode; // 1-jamming, 2-scanning
	int battery;
	int temp;
	int gpsState;						//0-unlock, 1-lock
	int airSyncState;					//2-async fail, 1-async ok   0：idle
	int autoCfgState;					//0-null, 1-ok, 2-fail, 3-auto_cfg_ing
	int pci;					//0--1007
	String wifiIp;
	String deviceName;
	String deviceId;
	// for glb msg
	int rspValue; // 配置成功与否,详见MessageProtocol: GR_2_UI_CFG_OK or GR_2_UI_CFG_NG

	// autoCfgState
	public static final int SFLAG_NULL = 0;       //
	public static final int SFLAG_AUTO_CFG_OK = 1;     //
	public static final int SFLAG_AUTO_CFG_FAIL = 2;     //
	public static final int SFLAG_IN_AUTO_CFG = 3;     //
	// workMode
	public static final int IDLE = 0;       //
	public static final int JAMMING = 1;     // 干扰
	public static final int PWR_DETECT = 2;     // 单兵检测

	public MsgStateRsp(int msgSn, int msgLen, int msgType, int param, int workMode,
					   int battery, int temp, int gps_state,int async_state, int pci, int autoCfgState, String wifi,String deviceName,String deviceId) {
		this.msgSn = msgSn;
		this.msgLen = msgLen;
		this.msgType = msgType;
		this.param = param;
		this.workMode = workMode;
		this.battery = battery;
		this.temp = temp;
		this.gpsState = gps_state;
		this.airSyncState = async_state;
		this.autoCfgState = autoCfgState;
		this.pci = pci;
		this.wifiIp = wifi;
		this.deviceName = deviceName;
		this.deviceId = deviceId;
		this.rspValue = -1;
	}

	public MsgStateRsp(int msgSn, int msgLen, int msgType, int rspValue) {
		this.msgSn = msgSn;
		this.msgLen = msgLen;
		this.msgType = msgType;
		this.rspValue = rspValue;
	}
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getAutoCfgState() {
		return autoCfgState;
	}

	public void setAutoCfgState(int autoCfgState) {
		this.autoCfgState = autoCfgState;
	}

	public int getPci() {
		return pci;
	}

	public void setPci(int pci) {
		this.pci = pci;
	}

	public int getGpsState() {
		return gpsState;
	}

	public void setGpsState(int gpsState) {
		this.gpsState = gpsState;
	}

	public int getAirSyncState() {
		return airSyncState;
	}

	public void setAirSyncState(int airSyncState) {
		this.airSyncState = airSyncState;
	}

	public int getTemp() {
		return temp;
	}

	public int getBattery() {
		return battery;
	}

	public void setBattery(int battery) {
		this.battery = battery;
	}

	public String getWifiIp() {
		return wifiIp;
	}

	public void setWifiIp(String wifiIp) {
		this.wifiIp = wifiIp;
	}

	public int getMsgSn() {
		return msgSn;
	}

	public int getMsgLen() {
		return msgLen;
	}

	public int getMsgType() {
		return msgType;
	}

	public int getParam() {
		return param;
	}

	public int getWorkMode() {
		return workMode;
	}

	public int getRspValue() {
		return rspValue;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@Override
	public String toString() {
		return "MsgStateRsp{" +
				"msgSn=" + msgSn +
				", msgLen=" + msgLen +
				", msgType=" + msgType +
				", workMode=" + workMode +
				", battery=" + battery +
				", temp=" + temp +
				", gpsState=" + gpsState +
				", airSyncState=" + airSyncState +
				", autoCfgState=" + autoCfgState +
				", pci=" + pci +
				", wifiIp=" + wifiIp +
				", deviceName=" + deviceName +
				", deviceId=" + deviceId +
				'}';
	}
	public String toCmdString() {
		return "MsgStateRsp{" +
				"msgSn=" + msgSn +
				", msgLen=" + msgLen +
				", msgType=" + msgType +
				", rspValue=" + rspValue +
				'}';
	}
}
