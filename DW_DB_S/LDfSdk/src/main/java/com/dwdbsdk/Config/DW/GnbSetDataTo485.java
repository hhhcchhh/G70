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
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.Util.DataUtil;

public class GnbSetDataTo485 {
	private final int u16MsgLength = DWHeader.headLength  + 4*5 + 256;/* 定义消息的长度 */
	private final byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbSetDataTo485(DWHeader header, int mod_id,int mod_addr,int cmd_id,int ack,int data_len,String data) {
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
		int2byte(mod_id);
		int2byte(mod_addr);
		int2byte(cmd_id);
		int2byte(ack);
		int2byte(data_len);
		handleDate(data);

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
	private void handleDate(String data) {
		int idx_start = 0, idx_end = 1;
		String a;
		while (idx_end <= data.length()) {
			String tmp = data.substring(idx_start, idx_end);
			sendMsg[mMsgIdx] = Byte.valueOf(tmp, 16);
			mMsgIdx++;
			idx_start++;
			idx_end++;
		}
		sendMsg[mMsgIdx] = 0;
		mMsgIdx++;
	}

	public byte[] getMsg() {
		return sendMsg;
	}
}
