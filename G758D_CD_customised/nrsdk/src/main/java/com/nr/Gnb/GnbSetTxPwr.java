/**
 * 设置功率衰减
 * 
 * UI_2_gNB_SET_TX_POWER_OFFSET = 12;  // 定位过程中用。
 * OAM_MSG_ADJUST_TX_ATTEN = 210;  // 出厂校准板子用。
 * typedef struct {
 * 		int sync_header;
 * 		int msg_type;         		//UI_2_gNB_OAM_MSG
 * 		int cmd_type;           	//UI_2_gNB_SET_TX_POWER_OFFSET || OAM_MSG_ADJUST_TX_ATTEN
 * 		int cmd_param;
 *
 *		int cell_id;
 * 		int nr_arfcn;           	// 需要衰减nr-arfcn, nv修正用BAND
 * 		int tx_atten;     			// 0~-15dB offset || 0~-30dB offset(NV)，衰减设置
 *
 * 		int sync_footer;
 * } tx_power_atten_t;		//in
 */
package com.nr.Gnb;


import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbSetTxPwr {
    public static int u16MsgLength = Header.headLength + 12;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;
	
	public GnbSetTxPwr(Header header, int cell_id, int nr_arfcn, int tx_atten) {
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}
		
		byte headMsg[] = header.getHeaderMsg();
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}

		int2byte(cell_id);
		int2byte(nr_arfcn);
		int2byte(tx_atten);

		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}

	private void int2byte(int idata) {
		byte[] data = DataUtil.intToBytes(idata);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	
	public byte[] getMsg() {
		return sendMsg;
	}
}
