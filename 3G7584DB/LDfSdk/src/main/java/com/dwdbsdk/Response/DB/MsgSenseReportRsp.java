/**
 * 数据传输
 * typedef struct {
 * 	int msg_header;						//HEADER_MAGIC
 * 	int msg_sn;							//serial num
 * 	int msg_len;						//sizeof bt_msg_xxx_t
 * 	int msg_type;						//BT_MSG_SENSE_REPORT = 0x36
 *
 *	int param;
 * 	int sensitivity;
 * 	int resv[4];
 * 	int msg_footer;						//FOOTER_MAGIC
 * } bt_msg_sense_report_t; //out
 */
package com.dwdbsdk.Response.DB;

public class MsgSenseReportRsp {
	int msgSn;							//serial num
	int msgLen;						//sizeof bt_msg_xxx_t
	int msgType;						//MessageProtocol.MsgType
	int sensitivity;
	MsgStateRsp stateRsp;

	public MsgSenseReportRsp() {
		this.sensitivity = 0;
		this.stateRsp = null;
	}

	public MsgSenseReportRsp(int msgSn, int msgLen, int msgType, int sensitivity) {
		this.msgSn = msgSn;
		this.msgLen = msgLen;
		this.msgType = msgType;
		this.sensitivity = sensitivity;
		this.stateRsp = null;
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

	public MsgStateRsp getStateRsp() {
		return stateRsp;
	}

	public void setStateRsp(MsgStateRsp stateRsp) {
		this.stateRsp = stateRsp;
	}

	public int getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(int data) {
		this.sensitivity = data;
	}

	@Override
	public String toString() {
		return "MsgReadDataFwdRsp{" +
				"msgSn=" + msgSn +
				", msgLen=" + msgLen +
				", msgType=" + msgType +
				", sensitivity='" + sensitivity + '\'' +
				'}';
	}
}
