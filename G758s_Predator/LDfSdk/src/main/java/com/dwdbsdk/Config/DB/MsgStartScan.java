/**
 * typedef struct {
 * int msg_header;      				//HEADER_MAGIC
 * int msg_sn;       					//serial num
 * int msg_len;      					//sizeof gr_msg_xxx_t
 * int msg_type;      				//GR_MSG_START_POS_SCAN
 * <p>
 * int param;
 * long freq_carrier;   //
 * int kssb;
 * int offset2pointA;
 * //int time_offset;
 * //int ue_position;    // 9--200
 * //int sched_mode;    					// 0: vehicle(40ms)   1: normal(160ms)
 * //int par_cfg; 						// real*100, 0 means default(300)
 * int msg_footer;      				//FOOTER_MAGIC
 * } gr_msg_start_scan_t;
 */
package com.dwdbsdk.Config.DB;

import com.dwdbsdk.Bean.DB.DBHeader;
import com.dwdbsdk.Util.DataUtil;

public class MsgStartScan {
    private final int u16MsgLength = DBHeader.headLength + 4 * 7 + 8;/* 定义消息的长度 */
    private final byte[] sendMsg = new byte[u16MsgLength];
    private int mMsgIdx;

    public MsgStartScan(int msg_type, long freq_carrier, int kssb, int offset2pointA) {
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
        long2byte(freq_carrier);
        int2byte(kssb);
        int2byte(offset2pointA);

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
