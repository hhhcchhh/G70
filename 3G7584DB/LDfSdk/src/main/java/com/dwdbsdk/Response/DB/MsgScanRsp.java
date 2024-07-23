/**
 typedef struct {
 int msg_header;						//HEADER_MAGIC
 int msg_sn;							//serial num
 int msg_len;						//sizeof gr_msg_xxx_t
 int msg_type;						//GR_MSG_SCAN_REPORT

 int result;   // SCAN result [0 : fail    1: succ]
 int lock_state;   // pwr detect [1: lock 0: unlock]
 int signal_power; // 0:  invalid
 int min_noise_power;
 int best_position;

 int msg_footer;						//FOOTER_MAGIC
 } gr_msg_scan_report_t;
 */
package com.dwdbsdk.Response.DB;

public class MsgScanRsp {
	int msgSn;							//serial num
	int msgLen;						//sizeof bt_msg_xxx_t
	int msgType;

	int lockState;		// 1: lock 0: unlock
	int rsrp;			//signal_power;		//lock_state = 1: 报值有效
	int rsrp_first;			//signal_power;		//lock_state = 1: 报值有效
	int rsrp_second;			//signal_power;		//lock_state = 1: 报值有效
	int scanResult;		//0: fail  1： succssful
	int minNoisePower;	//最小噪声
	int bestPosition;		//最佳UE SSB 位置
	MsgStateRsp stateRsp;   // 心跳

	//scanResult
	public static final int FAIL_BOTH = 0;
	public static final int SUCC_CELL_0 = 1;
	public static final int SUCC_CELL_1 = 2;
	public static final int SUCC_BOTH = 3;

	public MsgScanRsp() {
        this.scanResult = 0;
        this.lockState = 0;
        this.rsrp = 0;
        this.minNoisePower = 0;
		this.bestPosition = 0;

		this.stateRsp = null;
	}

	public MsgScanRsp(MsgStateRsp stateRsp) {
        this.scanResult = 0;
        this.lockState = 0;
        this.rsrp = 0;
        this.minNoisePower = 0;
		this.bestPosition = 0;

		this.stateRsp = stateRsp;
	}

	public int getMsgSn() {
		return msgSn;
	}

	public void setMsgSn(int msgSn) {
		this.msgSn = msgSn;
	}

	public int getMsgLen() {
		return msgLen;
	}

	public void setMsgLen(int msgLen) {
		this.msgLen = msgLen;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public int getLockState() {
		return lockState;
	}

	public void setLockState(int lockState) {
		this.lockState = lockState;
	}

	public int getScanResult() {
		return scanResult;
	}

	public void setScanResult(int scanResult) {
		this.scanResult = scanResult;
	}

	public int getRsrp() {
		return rsrp;
	}

	public void setRsrp(int rsrp) {
		this.rsrp = rsrp;
	}

	public int getRsrp_first() {
		return rsrp_first;
	}

	public void setRsrp_first(int rsrp_first) {
		this.rsrp_first = rsrp_first;
	}

	public int getRsrp_second() {
		return rsrp_second;
	}

	public void setRsrp_second(int rsrp_second) {
		this.rsrp_second = rsrp_second;
	}

	public int getMinNoisePower() {
		return minNoisePower;
	}

	public void setMinNoisePower(int minNoisePower) {
		this.minNoisePower = minNoisePower;
	}

	public int getBestPosition() {
		return bestPosition;
	}

	public void setBestPosition(int bestPosition) {
		this.bestPosition = bestPosition;
	}

	public MsgStateRsp getStateRsp() {
		return stateRsp;
	}

	public void setStateRsp(MsgStateRsp stateRsp) {
		this.stateRsp = stateRsp;
	}

	@Override
	public String toString() {
		return "MsgScanRsp{" +
				"msgSn=" + msgSn +
				", msgLen=" + msgLen +
				", msgType=" + msgType +
				", lockState=" + lockState +
//				", rsrp=" + rsrp +
				", rsrp_first=" + rsrp_first +
				", rsrp_second=" + rsrp_second +
				", scanResult=" + scanResult +
				", minNoisePower=" + minNoisePower +
				", bestPosition=" + bestPosition +
				'}';
	}
}
