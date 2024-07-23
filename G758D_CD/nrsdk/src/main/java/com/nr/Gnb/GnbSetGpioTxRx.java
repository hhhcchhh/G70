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
import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

import java.util.Arrays;

public class GnbSetGpioTxRx {
	public static int u16MsgLength = Header.headLength  + 4*8;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbSetGpioTxRx(Header header, int[] gpio) {
		SLog.D("GnbSetGpioTxRx gpio = " + Arrays.toString(gpio));
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
		for (int i = 0; i < gpio.length; i++) {
			int2byte(gpio[i]);

		}
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
	private void handleUserData(String user_data) {
		byte[] bytes = user_data.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			sendMsg[mMsgIdx] = bytes[i];
			mMsgIdx++;
		}
		if (bytes.length < 256) {
			int offset = 256 - bytes.length;
			mMsgIdx += offset;
		}
	}

	public byte[] getMsg() {
		return sendMsg;
	}
}
