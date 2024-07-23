/**
 * 配置工作模式
 * 
 * UI_2_gNB_SET_MODE = 19;  //
 */
package com.nr.Gnb;

import com.nr.Util.DataUtil;
import com.nr.Gnb.Bean.Header;

public class GnbSetMode {
	private Header header;// 共8字节 消息头、消息类型、消息尾/
	private int mode;                // 0: single; 1: vehicle

    public static int u16MsgLength = Header.headLength + 4;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx = 0;

	public GnbSetMode(Header header, int mode) {
		super();
		this.header = header;
		this.mode = mode;

		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}
		
		byte headMsg[] = header.getHeaderAndMsgType(u16MsgLength);
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}
		
		handleSetMode();
		
		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}

	private void handleSetMode() {
		byte[] data = DataUtil.intToBytes(mode);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	
	public byte[] getMsg() {
		return sendMsg;
	}
}
