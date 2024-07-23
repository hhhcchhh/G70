/**
 * 基带版本信息
 */
package com.dwdbsdk.Response.DB;

public class MsgVersionRsp {
	int msgSn;							//serial num
	int msgLen;						//sizeof bt_msg_xxx_t
	int msgType;						//MessageProtocol.MsgType
	String version;
	MsgStateRsp stateRsp;

	public MsgVersionRsp() {
		this.version = null;
		this.stateRsp = null;
	}

	public MsgVersionRsp(int msgSn, int msgLen, int msgType, String version) {
		this.msgSn = msgSn;
		this.msgLen = msgLen;
		this.msgType = msgType;
		this.version = version;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "MsgVersionRsp{" +
				"msgSn=" + msgSn +
				", msgLen=" + msgLen +
				", msgType=" + msgType +
				", version='" + version + '\'' +
				'}';
	}
}
