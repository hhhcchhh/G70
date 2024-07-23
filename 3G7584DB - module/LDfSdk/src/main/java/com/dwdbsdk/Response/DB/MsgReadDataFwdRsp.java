/**
 * 基带版本信息
 */
package com.dwdbsdk.Response.DB;

public class MsgReadDataFwdRsp {
	int msgSn;							//serial num
	int msgLen;						//sizeof bt_msg_xxx_t
	int msgType;						//MessageProtocol.MsgType
	String data;
	MsgStateRsp stateRsp;

	public MsgReadDataFwdRsp() {
		this.data = null;
		this.stateRsp = null;
	}

	public MsgReadDataFwdRsp(int msgSn, int msgLen, int msgType, String data) {
		this.msgSn = msgSn;
		this.msgLen = msgLen;
		this.msgType = msgType;
		this.data = data;
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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "MsgReadDataFwdRsp{" +
				"msgSn=" + msgSn +
				", msgLen=" + msgLen +
				", msgType=" + msgType +
				", data='" + data + '\'' +
				'}';
	}
}
