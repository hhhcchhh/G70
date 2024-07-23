/**
 * 读取基带LOG
 typedef struct {
 	int sync_header;
 	int msg_type;    	//UI_2_gNB_OAM_MSG
 	int cmd_type;	 	// UI_2_gNB_GET_LOG_REQ, UI_2_gNB_GET_OP_LOG_REQ
 	int cmd_param;

 	int server_type; 	// tftp server: 0; ftp server: 1; serial: 2; scp: 3
 	char log_name[64];

 	int sync_footer;
 } oam_get_log_t; //in
 */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.Logcat.SLog;
import com.nr.Util.DataUtil;

public class GnbGetLog {
	public static int u16MsgLength = Header.headLength  + 4 + 64;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbGetLog(Header header, int server_type, String log_name) {
		SLog.D("gnbGetLog type = " + server_type + ", name = " + log_name);
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
		int2byte(server_type);
		handleLogName(log_name);

		mMsgIdx = u16MsgLength - 4;
		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}

	private void int2byte(int idata) {
		byte[] data = DataUtil.intToBytes(idata);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}

	private void handleLogName(String log_name) {
		byte[] bytes = log_name.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			sendMsg[mMsgIdx] = bytes[i];
			mMsgIdx++;
		}
	}

	public byte[] getMsg() {
		return sendMsg;
	}
}
