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
package com.dwdbsdk.Config.DW;
import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

public class GnbGetLog {
	private final int u16MsgLength = DWHeader.headLength  + 4 + 64;/* 定义消息的长度 */
	private final byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbGetLog(DWHeader header, int server_type, String log_name) {
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}

		byte[] headMsg = header.getHeaderMsg();
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}
		
		// 此处顺序不能变
		int2byte(server_type);
		handleLogName(log_name);

		mMsgIdx = u16MsgLength - 4;
		byte[] footerMsg = header.getFooterMsg();
		for (byte b : footerMsg) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
	}

	private void int2byte(int idata) {
		byte[] data = DataUtil.intToBytes(idata);
		for (byte datum : data) {
			sendMsg[mMsgIdx] = datum;
			mMsgIdx++;
		}
	}

	private void handleLogName(String log_name) {
		byte[] bytes = log_name.getBytes();
		for (byte aByte : bytes) {
			sendMsg[mMsgIdx] = aByte;
			mMsgIdx++;
		}
	}

	public byte[] getMsg() {
		return sendMsg;
	}
}
