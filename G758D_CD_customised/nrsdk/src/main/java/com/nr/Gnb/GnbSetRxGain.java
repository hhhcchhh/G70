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
package com.nr.Gnb;

import com.nr.Gnb.Bean.Header;
import com.nr.Util.DataUtil;

public class GnbSetRxGain {
    public static int u16MsgLength = Header.headLength + 8;/* 定义消息的长度 */
    private byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public GnbSetRxGain(Header header, int chan_id, int rx_gain) {
        mMsgIdx = 0;
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }

        byte headMsg[] = header.getHeaderMsg();
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }

        int2byte(chan_id);
        int2byte(rx_gain);

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
