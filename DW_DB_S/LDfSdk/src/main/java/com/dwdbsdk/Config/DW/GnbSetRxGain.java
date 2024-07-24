/*
* typedef struct {
 int sync_header;
 int msg_type;           //UI_2_gNB_OAM_MSG(in)
 int cmd_type;             //OAM_MSG_SET_RX_GAIN
 int cmd_param;

 int cell_id;           //0：通道一 1：通道二
 int rx_gain;             //0~30db
 int sync_footer;
} oam_rx_gain_t;
* */
package com.dwdbsdk.Config.DW;

import com.dwdbsdk.Bean.DW.DWHeader;
import com.dwdbsdk.Util.DataUtil;

public class GnbSetRxGain {
    private final int u16MsgLength = DWHeader.headLength + 8;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetRxGain(DWHeader header, int chan_id, int rx_gain) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte[] headMsg = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        int2byte(chan_id);
        int2byte(rx_gain);

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
