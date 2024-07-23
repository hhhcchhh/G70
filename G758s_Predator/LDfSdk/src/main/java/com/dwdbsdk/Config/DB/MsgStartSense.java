package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.Util.DataUtil;

/**
 typedef struct {
 int msg_header;      				//HEADER_MAGIC
 int msg_sn;       				//serial num
 int msg_len;      				//sizeof bt_msg_xxx_t
 int msg_type;      				//BT_MSG_START_SENSE_DET = 0x42

 int param;
 int dl_arfcn;
 int ul_arfcn;
 int kssb;
 int offset2pointA;
 int time_offset;
 int resv[4];

 int msg_footer;
 } bt_msg_start_sense_det_t; //in
 */

public class MsgStartSense {
	public static int u16MsgLength = DBHeader.headLength  + 4*10;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public MsgStartSense(int msg_type, int dl_arfcn, int ul_arfcn, int kssb, int offset2pointA, int time_offset) {
		SdkLog.D("MsgStartSense  [ " + dl_arfcn + ", " + ul_arfcn + ", " + kssb + ", " + offset2pointA + ", " + time_offset  + " ]");
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}
		mMsgIdx = 0;
		byte headMsg[] = DBHeader.getHeader(u16MsgLength, msg_type);
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[mMsgIdx] = headMsg[i];
			mMsgIdx++;
		}
		// 此处顺序不能变
		int2byte(0); // param
		int2byte(dl_arfcn);
		int2byte(ul_arfcn);
		int2byte(kssb);
		int2byte(offset2pointA);
		int2byte(time_offset);

		mMsgIdx = u16MsgLength - 4;
		byte footerMsg[] = DBHeader.getFooter();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}
	
	private void str2byte(String content) {
		byte data[] = content.getBytes();
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
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

	public byte[] getMsg() {
		return sendMsg;
	}
}
