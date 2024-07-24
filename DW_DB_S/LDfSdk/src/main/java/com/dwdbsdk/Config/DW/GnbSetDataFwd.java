package com.dwdbsdk.Config.DW;
/**
 1、主机通过串口发给单板
 2、单板通过wifi发给app
 3、app通过wifi发给单板
 4、单板通过串口发给主机
 typedef struct {
 int sync_header;
 int msg_type;           //UI_2_gNB_OAM_MSG
 int cmd_type;          //301
 int param;				//>>datalen payload + 20

 char payload[256];		//0-256字节
 int msg_footer;		//FOOTER_MAGIC=0x55AA3333
 } bt_msg_data_fwd_t;
 */
import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Bean.DB.DBProtocol;
import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Util.DataUtil;

public class GnbSetDataFwd {
	private final int u16MsgLength = DWHeader.headLength  + 256;/* 定义消息的长度 */
	private final byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbSetDataFwd(DWHeader header, String data) {
		byte[] bData = data.getBytes();
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}
		byte[] headMsg = header.getHeaderMsg();
		for (byte b : headMsg) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
		// 此处顺序不能变
//		int2byte(0); // param

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
		byte[] DBhead = DBHeader.getHeader(DBHeader.headLength+4+bData.length, DBProtocol.MsgType.GR_MSG_DATA_FWD);
		for (byte b : DBhead) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
		int2byte(0); // param
		for (byte bDatum : bData) {
			sendMsg[mMsgIdx] = bDatum;
			mMsgIdx++;
		}

		byte[] DbFooterMsg = DBHeader.getFooter();
		for (byte b : DbFooterMsg) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
		mMsgIdx = u16MsgLength - 4;
		byte[] footerMsg = header.getFooterMsg();
		for (byte b : footerMsg) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
	}
	public GnbSetDataFwd(DWHeader header, byte[] bData) {
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}
		byte[] headMsg = header.getHeaderMsg();
		for (byte b : headMsg) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}

		byte[] DBhead = DBHeader.getHeader(DBHeader.headLength+4+bData.length, DBProtocol.MsgType.GR_MSG_DATA_FWD);
		for (byte b : DBhead) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
		int2byte(0); // param
		for (byte bDatum : bData) {
			sendMsg[mMsgIdx] = bDatum;
			mMsgIdx++;
		}

		byte[] DbFooterMsg = DBHeader.getFooter();
		for (byte b : DbFooterMsg) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
		mMsgIdx = u16MsgLength - 4;
		byte[] footerMsg = header.getFooterMsg();
		for (byte b : footerMsg) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
	}

	public GnbSetDataFwd(DWHeader header, char[] bData) {
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}
		byte[] headMsg = header.getHeaderMsg();
		for (byte b : headMsg) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
		byte[] DBhead = DBHeader.getHeader(DBHeader.headLength+4+bData.length, DBProtocol.MsgType.GR_MSG_DATA_FWD);
		for (byte b : DBhead) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
		int2byte(0); // param
		for (char bDatum : bData) char2byte(bDatum);

		byte[] DbFooterMsg = DBHeader.getFooter();
		for (byte b : DbFooterMsg) {
			sendMsg[mMsgIdx] = b;
			mMsgIdx++;
		}
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
