/**
 * 设置功率衰减
 * <p>
 * UI_2_gNB_SET_TX_POWER_OFFSET = 12;  // 定位过程中用。
 * OAM_MSG_ADJUST_TX_ATTEN = 210;  // 出厂校准板子用。
 * ## 这样配置文件有两种格式，频点优先于频段
 * 基于频段配置：tx1_78 = -2
 * 基于频点配置：tx1_freq_627264 = -1
 * typedef struct {
 * int sync_header;
 * int msg_type;         		//UI_2_gNB_OAM_MSG
 * int cmd_type;           	//UI_2_gNB_SET_TX_POWER_OFFSET || OAM_MSG_ADJUST_TX_ATTEN
 * int cmd_param;				//0-nr_arfcn为频段，1-nr_arfcn为频点
 * <p>
 * int cell_id;
 * int nr_arfcn;           	// 需要衰减nr-arfcn, nv修正用BAND
 * int tx_atten;     			// 0~-15dB offset || 0~-30dB offset(NV)，衰减设置
 * <p>
 * int sync_footer;
 * } tx_power_atten_t;		//in
 */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

public class GnbSetTxPwr {
    private final int u16MsgLength = DWHeader.headLength + 12;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetTxPwr(DWHeader header, int cell_id, int nr_arfcn, int tx_atten) {
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
        int2byte(nr_arfcn);
        int2byte(tx_atten);

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
