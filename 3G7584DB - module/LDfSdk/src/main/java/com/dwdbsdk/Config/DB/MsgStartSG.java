/**
 * typedef struct {
 * int msg_header;          //HEADER_MAGIC
 * int msg_sn;            //serial num
 * int msg_len;           //sizeof bt_msg_xxx_t
 * int msg_type;           //BT_MSG_START_SG
 * int param;
 * <p>
 * int tx_chan;      //unused
 * long lo_frequency;      //3450000000
 * int tx_atten;      //0-30db
 * int sg_type;      //0-WhiteNoise_100MHz, 1-Tone_10MHz
 * int msg_footer;          //FOOTER_MAGIC
 * } bt_msg_start_sg_t; //in
 */
package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Util.DataUtil;

public class MsgStartSG {
    private final int u16MsgLength = DBHeader.headLength + 4 * 7 + 8;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public MsgStartSG(int msg_type, int tx_chan, long lo_frequency, int tx_atten, int sg_type) {
        for (int i = 0; i < u16MsgLength; i++) {
            sendMsg[i] = 0;
        }
        mMsgIdx = 0;
        byte[] headMsg = DBHeader.getHeader(u16MsgLength, msg_type);
        for (int i = 0; i < headMsg.length; i++) {
            sendMsg[i] = headMsg[i];
            mMsgIdx++;
        }
        // 此处顺序不能变
        int2byte(0);
        int2byte(tx_chan);
        long2byte(lo_frequency);
        int2byte(tx_atten);
        int2byte(sg_type);

        mMsgIdx = u16MsgLength - 4;
        byte[] footerMsg = DBHeader.getFooter();
        for (byte b : footerMsg) {
            sendMsg[mMsgIdx] = b;
            mMsgIdx++;
        }
    }

    private void int2byte(int para) {
        byte[] data = DataUtil.intToBytes(para);
        for (byte datum : data) {
            sendMsg[mMsgIdx] = datum;
            mMsgIdx++;
        }
    }

    private void long2byte(long para) {
        byte[] data = DataUtil.long2Bytes(para);
        for (byte datum : data) {
            sendMsg[mMsgIdx] = datum;
            mMsgIdx++;
        }
    }

    public byte[] getMsg() {
        return sendMsg;
    }
}
