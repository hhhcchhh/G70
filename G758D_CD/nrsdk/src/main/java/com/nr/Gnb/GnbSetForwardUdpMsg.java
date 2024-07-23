/**
 * 设置功率衰减
 * 
 * UI_2_gNB_SET_TX_POWER_OFFSET = 12;  // 定位过程中用。
 * OAM_MSG_ADJUST_TX_ATTEN = 210;  // 出厂校准板子用。
 * ## 这样配置文件有两种格式，频点优先于频段
 * 基于频段配置：tx1_78 = -2
 * 基于频点配置：tx1_freq_627264 = -1
 * typedef struct {
 * 		int sync_header;
 * 		int msg_type;         		//UI_2_gNB_OAM_MSG
 * 		int cmd_type;           	//UI_2_gNB_SET_TX_POWER_OFFSET || OAM_MSG_ADJUST_TX_ATTEN
 * 		int cmd_param;				//0-nr_arfcn为频段，1-nr_arfcn为频点
 *
 *		int cell_id;
 * 		int nr_arfcn;           	// 需要衰减nr-arfcn, nv修正用BAND
 * 		int tx_atten;     			// 0~-15dB offset || 0~-30dB offset(NV)，衰减设置
 *
 * 		int sync_footer;
 * } tx_power_atten_t;		//in
 */
package com.nr.Gnb;


import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbSetForwardUdpMsg {
    public static int u16MsgLength = Header.headLength + 2 * GnbProtocol.OAM_STR_MAX + 4;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx;

	public GnbSetForwardUdpMsg(Header header, String dst_ip, int dst_port, String fwd_info) {
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
		handleStr(dst_ip);
		int2byte(dst_port);
		handleStr(fwd_info);

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
