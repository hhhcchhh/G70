/**
 * 基带板参数配置
 * UI_2_gNB_SUB_HEART = 500
 */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;

public class GndSubHeartCfg {
	private Header header;// 共8字节 消息头、消息类型、消息尾/
	private int async_state;
	//private int reserve[15];     //32-9
	private int magic;   //set as 0x11223344

	public static int u16MsgLength = Header.headLength + 8 + 15*4;
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx = 0;

	public GndSubHeartCfg(Header header) {
		this.header = header;
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}

		byte headMsg[] = header.getHeaderAndMsgType(u16MsgLength);
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}
		// 以下顺序不能改变
		// async_state
		for (int i = 0; i < 16*4; i++) {
			sendMsg[mMsgIdx] = 0;
			mMsgIdx++;
		}
		//magic
		sendMsg[mMsgIdx] = (byte)0x44; /* 0x11223344*/
		mMsgIdx++;
		sendMsg[mMsgIdx] = 0x33;
		mMsgIdx++;
		sendMsg[mMsgIdx] = 0x22;
		mMsgIdx++;
		sendMsg[mMsgIdx] = 0x11;
		mMsgIdx++;

		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}

	public byte[] getMsg() {
		return sendMsg;
	}
}
