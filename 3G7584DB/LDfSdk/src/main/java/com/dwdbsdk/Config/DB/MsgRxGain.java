package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Util.DataUtil;

/**
 * typedef struct {
 * int msg_header;      //HEADER_MAGIC
 * int msg_sn;       //serial num
 * int msg_len;      //sizeof bt_msg_xxx_t
 * int msg_type;      //BT_MSG_RX_GAIN(0x0040)
 * <p>
 * int param;
 * int rx_gain;      //0-default(far), 1-near
 * <p>
 * int msg_footer;      //FOOTER_MAGIC
 * } bt_msg_cfg_rx_gain_t; //down
 */

public class MsgRxGain {
    private final int u16MsgLength = DBHeader.headLength + 8;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public MsgRxGain(int msg_type, int rx_gain) {
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }
        mMsgIdx = 0;
        byte[] headMsg = DBHeader.getHeader(u16MsgLength, msg_type);
        for (byte b : headMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
        // 此处顺序不能变
        int2byte(0); // param
        int2byte(rx_gain);

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = DBHeader.getFooter();
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
