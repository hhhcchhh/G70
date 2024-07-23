/**
 * 单板IP及路由器IP及端口配置
 *
 * typedef struct {
 * 		int sync_header;
 * 		int msg_type;          		//UI_2_gNB_OAM_MSG
 * 		int cmd_type;             	//CGI_MSG_SET_METH_CFG
 * 		int cmd_param;
 *
 * 		char meth_ip[OAM_STR_MAX]; // "192.168.1.33"
 * 		char meth_mask[OAM_STR_MAX]; // "255.255.255.0"
 * 		char meth_gw[OAM_STR_MAX]; // "192.168.1.1"
 *
 * 		int sync_footer;
 * } oam_set_meth_t; 	//in
 */
package com.nr.Gnb;

import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.Header;

public class GnbMethIpCfg {

	public static int u16MsgLength = Header.headLength  + 4* GnbProtocol.OAM_STR_MAX;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx = 0;

	public GnbMethIpCfg(Header header, String meth_ip, String meth_mask, String meth_gw, String meth_mac) {
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
		handleStr(meth_ip);
		handleStr(meth_mask);
		handleStr(meth_gw);
		handleStr(meth_mac);

		mMsgIdx = u16MsgLength - 4;
		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}
	
	private void handleStr(String meth_ip) {
		byte[] bytes = meth_ip.getBytes();
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
