/**
 请求
 typedef struct {
 int sync_header;
 int msg_type;         		//UI_2_gNB_OAM_MSG
 int cmd_type;            	//UI_2_eNB_SET_PHONE_TYPE(in) = 170
 int cmd_param;

 int cell_id;
 int rnti;                   // from ue_report_t
 int resv[8];
 int sync_footer;
 } oam_phone_type_cfg_t;


 应答：
 typedef struct {
 int sync_header;
 int msg_type;          	//UI_2_gNB_OAM_MSG
 int cmd_type;
 int cmd_result;
 int cell_id;
 int sync_footer;
 } new_oam_msg2_t
 */
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbPhoneType {
    public static int u16MsgLength = Header.headLength + 40;/* 定义消息的长度 */
	private final byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbPhoneType(Header header, int cell_id, int rnti) {
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}
		
		byte[] headMsg = header.getHeaderMsg();
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}

		int2byte(cell_id);
		int2byte(rnti);

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
	
	public byte[] getMsg() {
		return sendMsg;
	}
}
