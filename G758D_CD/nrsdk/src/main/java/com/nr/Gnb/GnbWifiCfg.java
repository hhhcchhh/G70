/**
 * 设置单板WIFI工作模式、名称、密码
 *
 * typedef struct {
 * 		int sync_header;
 * 		int msg_type;    			//UI_2_gNB_OAM_MSG
 * 		int cmd_type;            	//UI_2_gNB_WIFI_CFG
 * 		int cmd_param;
 *
 * 		char ssid[OAM_STR_MAX];
 * 		char passwd[OAM_STR_MAX];
 *
 * 		int sync_footer;
 * } oam_wifi_cfg_t; //in
 */
package com.nr.Gnb;

import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.Header;

public class GnbWifiCfg {
	public static int u16MsgLength = Header.headLength  + 2*GnbProtocol.OAM_STR_MAX;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbWifiCfg(Header header, String ssid, String passwd) {
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
		handleSsid(ssid);
		handlePasswd(passwd);

		mMsgIdx = u16MsgLength - 4;
		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}
	
	private void handleSsid(String ssid) {
		byte[] bytes = ssid.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			sendMsg[mMsgIdx] = bytes[i];
			mMsgIdx++;
		}
		if (bytes.length < GnbProtocol.OAM_STR_MAX) {
			int offset = GnbProtocol.OAM_STR_MAX - bytes.length;
			mMsgIdx += offset;
		}
	}
	private void handlePasswd(String passwd) {
		byte[] bytes = passwd.getBytes();
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
