/**
 * 基带板时间配置同步LOG时间用
 *
 * UI_2_gNB_SET_TIME = 9
 */
package com.nr.Gnb;

import com.Logcat.SLog;
import com.nr.Gnb.Bean.Header;

public class GnbSetTime {
	private Header header;// 共8字节 消息头、消息类型、消息尾/
	//设置单板时间。数据格式为"2006-4-20 20:30:30"
	// byte date_time[] = new byte[20];
	private String date_time; // 20个字节
	 
	public static int u16MsgLength = Header.headLength  + 20;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx = 0;

	public GnbSetTime(Header header, String date_time) {
		super();
		this.header = header;
		this.date_time = date_time;
		SLog.D("GnbSetTime time = " + date_time);
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
		handleDateTime();
		mMsgIdx = u16MsgLength - 4;
		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}
	
	private void handleDateTime() {
		int idx_start = 0, idx_end = 1;
		String a;
		while (idx_end <= date_time.length()) {
			String tmp = date_time.substring(idx_start, idx_end);
			if (tmp.equals(".")) {
				a = "2e";
			} else if (tmp.equals("-")) {
				a = "2d";
			} else if (tmp.equals(":")) {
				a = "3a";
			} else if (tmp.equals(" ")) {
				a = "20";
			} else {
				a = "3" + tmp;
			}
			sendMsg[mMsgIdx] = Byte.valueOf(a, 16);
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
