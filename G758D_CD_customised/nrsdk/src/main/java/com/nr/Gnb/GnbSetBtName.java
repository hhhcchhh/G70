/**
 * 基带板时间配置同步LOG时间用
 typedef struct {
 	int sync_header;
 	int msg_type;    			//UI_2_gNB_OAM_MSG
 	int cmd_type;            	//UI_2_gNB_SET_TIME
 	int cmd_param;
	// OAM_STR_MAX = 32
 	char adapter_name[OAM_STR_MAX];

 	int sync_footer;
 } oam_set_time_t; //in
 */
package com.nr.Gnb;

import com.Logcat.SLog;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.Header;

public class GnbSetBtName {
	public static int u16MsgLength = Header.headLength  + GnbProtocol.OAM_STR_MAX;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbSetBtName(Header header, String adapter_name) {
		SLog.D("GnbSetBtName adapter_name = " + adapter_name);
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}

		byte headMsg[] = header.getHeaderMsg();
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}
		// 此处顺序不能变
		handleBtName(adapter_name);

		mMsgIdx = u16MsgLength - 4;
		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}
	
	private void handleBtName(String adapter_name) {
		byte[] bytes = adapter_name.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			sendMsg[mMsgIdx] = bytes[i];
			mMsgIdx++;
		}
		if (bytes.length < GnbProtocol.OAM_STR_MAX) {
			int offset = GnbProtocol.OAM_STR_MAX - bytes.length;
			mMsgIdx += offset;
		}
	}

	public byte[] getMsg() {
		return sendMsg;
	}
}
