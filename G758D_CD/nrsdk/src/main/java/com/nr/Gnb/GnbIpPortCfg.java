/**
 * 单板IP及路由器IP及端口配置
 *
 * UI_2_gNB_SET_UI_IP_PORT = 51
 * UI_2_gNB_SET_BOARD_IP_PORT = 50
 */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbIpPortCfg {
	private Header header;// 共8字节 消息头、消息类型、消息尾/
	private int ip;
	private int port;

	public static int u16MsgLength = Header.headLength  + 8;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx = 0;

	public GnbIpPortCfg(Header header, int ip, int port) {
		super();
		this.header = header;
		this.ip = ip;
		this.port = port;
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}

		byte headMsg[] = header.getHeaderAndMsgType(u16MsgLength);
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}
		// 此处顺序不能变
		handleIp();
		handlePort();

		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}
	
	private void handleIp() {
		byte[] data = DataUtil.intToBytes(ip);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	private void handlePort() {
		byte[] data = DataUtil.intToBytes(port);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}

	public byte[] getMsg() {
		return sendMsg;
	}
}
