/**
 * 设置授权时间
 typedef struct {
 int sync_header;
 int msg_type;    		//UI_2_gNB_OAM_MSG
 int cmd_type;	 	//OAM_MSG_SET_LIC_INFO=239
 int cmd_param;

 int lic_enable;		 // 是否生效  0：不生效  1：生效
 int expired_day;        //20240101, 0表示无日期限制
 int credit_max;         //可使用时间，分钟为单位，0表示无使用时间限制
 int resv[8];
 int sync_footer;
 } oam_lic_config_t;
 */
package com.dwdbsdk.Config.DW;


import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

public class GnbCfgLic {
    public int u16MsgLength = DWHeader.headLength + 11 * 4;/* 定义消息的长度 */
	private final byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbCfgLic(DWHeader header, int lic_enable, int expired_day, int credit_max) {
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}
		
		byte[] headMsg = header.getHeaderMsg();
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}

		int2byte(lic_enable);
		int2byte(expired_day);
		int2byte(credit_max * 60); // 基带板按分钟计算，传小时进来，在这转换为分钟

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
