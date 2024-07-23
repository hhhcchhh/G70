/**
 typedef struct {
 	int sync_header;
 	int msg_type;    		//UI_2_gNB_OAM_MSG
 	int cmd_type;           //UI_2_gNB_WRITE_OP_RECORD
 	int cmd_param;

 	char record[256];        // 一行文本记录，不超过250字节: [time_stamp] operation record

 	int sync_footer;
 } oam_record_write_t; //in
 */
package com.nr.Gnb;

import com.Logcat.SLog;
import com.nr.Gnb.Bean.Header;

public class GnbWriteOpLog {
	public static int u16MsgLength = Header.headLength  + 256;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbWriteOpLog(Header header, String record) {
		SLog.D("GnbWriteOpLog record = " + record);
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
		handleRecord(record);

		mMsgIdx = u16MsgLength - 4;
		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}
	
	private void handleRecord(String record) {
		byte[] bytes = record.getBytes();
		SLog.I("GnbWriteOpLog  bytes.length = "+bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			sendMsg[mMsgIdx] = bytes[i];
			mMsgIdx++;
		}
	}

	public byte[] getMsg() {
		return sendMsg;
	}
}
