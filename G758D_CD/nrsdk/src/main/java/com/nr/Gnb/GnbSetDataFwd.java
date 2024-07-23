package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

/**
 1、主机通过串口发给单板
 2、单板通过wifi发给app
 3、app通过wifi发给单板
 4、单板通过串口发给主机
 typedef struct {
 int msg_header;		//HEADER_MAGIC=0x5AA51111
 int msg_sn;		//serial num, 递增序号或填0
 int msg_len;		//payload_len + 24
 int msg_type;		//301
 int param;

 char payload[256];		//0-256字节
 int msg_footer;		//FOOTER_MAGIC=0x55AA3333
 } bt_msg_data_fwd_t;
 */

public class GnbSetDataFwd {
	// char bt_name[32];
	private final int u16MsgLength;/* 定义消息的长度 */
	private final byte[] sendMsg;
	private int mMsgIdx;

	public GnbSetDataFwd(Header header, String data) {

		byte[] bData = data.getBytes();
		u16MsgLength = Header.headLength + bData.length;/* 定义消息的长度 */
		sendMsg = new byte[u16MsgLength];
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}

		byte[] headMsg = header.getHeaderMsg();
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}

		for (byte bDatum : bData) {
			sendMsg[mMsgIdx] = bDatum;
			mMsgIdx++;
		}

		mMsgIdx = u16MsgLength - 4;
		byte[] footerMsg = header.getFooterMsg();
		for (byte b : footerMsg) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
	}

	public GnbSetDataFwd(Header header, byte[] bData) {

		u16MsgLength = Header.headLength  + bData.length;/* 定义消息的长度 */
		sendMsg = new byte[u16MsgLength];
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}

		byte[] headMsg = header.getHeaderMsg();
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}

		for (byte bDatum : bData) {
			sendMsg[mMsgIdx] = bDatum;
			mMsgIdx++;
		}

		mMsgIdx = u16MsgLength - 4;
		byte[] footerMsg = header.getFooterMsg();
		for (byte b : footerMsg) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
	}

	public GnbSetDataFwd(Header header, char[] bData) {
		u16MsgLength = Header.headLength + bData.length * 2;/* 定义消息的长度 */
		sendMsg = new byte[u16MsgLength];
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}

		byte[] headMsg = header.getHeaderMsg();
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}

		for (char bDatum : bData) char2byte(bDatum);
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
	private void char2byte(char cdata) {
		byte[] data = DataUtil.charToBytes(cdata);
		for (byte datum : data) {
			sendMsg[mMsgIdx] = datum;
			mMsgIdx++;
		}
	}

	public byte[] getMsg() {
		return sendMsg;
	}
}
