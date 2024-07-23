/**
 * 心跳数据 与 配置信息反馈
 */
package com.dwdbsdk.Response.DB;

public class MsgCmdRsp {
	int msgSn;						//serial num
	int msgLen;						//sizeof bt_msg_xxx_t
	int msgType;					//MessageProtocol.MsgType
	int rspValue; 					// 配置成功与否,详见MessageProtocol: GR_2_UI_CFG_OK or GR_2_UI_CFG_NG
	public MsgCmdRsp(int msgSn, int msgLen, int msgType, int rspValue) {
		this.msgSn = msgSn;
		this.msgLen = msgLen;
		this.msgType = msgType;
		this.rspValue = rspValue;
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

	public int getRspValue() {
		return rspValue;
	}

	public void setRspValue(int rspValue) {
		this.rspValue = rspValue;
	}

	public String toString() {
		return "MsgStateRsp{" +
				"msgSn=" + msgSn +
				", msgLen=" + msgLen +
				", msgType=" + msgType +
				", rspValue=" + rspValue +
				'}';
	}
}
