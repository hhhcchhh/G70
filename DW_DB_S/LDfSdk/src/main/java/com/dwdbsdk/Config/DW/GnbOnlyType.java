/**
 * 配置不带参数的指令
 *
 * UI_2_gNB_DELETE_OP_LOG_REQ = 7
 * UI_2_gNB_START_CATCH = 13
 * UI_2_gNB_STOP_CATCH = 14
 * UI_2_gNB_STOP_TRACE = 16
 * UI_2_gNB_REBOOT_gNB = 17
 * UI_2_gNB_QUERY_gNB_VERSION = 18
 */
package com.dwdbsdk.Config.DW;
import com.dwdbsdk.Bean.DW.DWHeader;

public class GnbOnlyType {
	private final int u16MsgLength = DWHeader.headLength;/* 定义消息的长度 */
	private final byte[] sendMsg = new byte[u16MsgLength];

	public GnbOnlyType(DWHeader header) {
		int mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}

		byte[] headMsg = header.getHeaderMsg();
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}

		byte[] footerMsg = header.getFooterMsg();
		for (byte b : footerMsg) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
	}

	public byte[] getMsg() {
		return sendMsg;
	}
}
